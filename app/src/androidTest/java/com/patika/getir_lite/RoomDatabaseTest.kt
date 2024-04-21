package com.patika.getir_lite

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.patika.getir_lite.data.local.ProductDao
import com.patika.getir_lite.data.local.ProductDatabase
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.fake.data.fakeProductEntities
import com.patika.getir_lite.model.ProductType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var database: ProductDatabase
    private lateinit var productDao: ProductDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, ProductDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = database.productDto()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun setupProductAndOrder(
        productId: Long,
        orderId: Long,
        price: BigDecimal,
        count: Int = 0
    ) {
        productDao.insertOrder(
            OrderEntity(
                orderId,
                OrderStatus.ON_BASKET,
                price.multiply(BigDecimal(count))
            )
        )
        productDao.insertProducts(
            listOf(
                ProductEntity(
                    id = productId,
                    productId = "001",
                    name = "Test Product",
                    price = price,
                    productType = ProductType.PRODUCT,
                    attribute = ""
                )
            )
        )
        if (count > 0) {
            productDao.insertItem(
                ItemEntity(
                    productId = productId,
                    orderId = orderId,
                    count = count
                )
            )
        }
    }

    @Test
    fun insertOrder_savesOrderCorrectly() = runTest {
        val orderEntity =
            OrderEntity(id = 1, orderStatus = OrderStatus.ON_BASKET, totalPrice = BigDecimal.ZERO)
        val id = productDao.insertOrder(orderEntity)
        assertEquals(1L, id)
        val loaded = productDao.getOrderById(id)
        assertEquals(orderEntity, loaded)
    }

    @Test
    fun insertProducts_savesProductsCorrectly() = runTest {
        val products = fakeProductEntities
        productDao.insertProducts(products)
        val loaded = productDao.getAllItems()
        assertEquals(products, loaded)
    }

    @Test
    fun getProductsWithCounts_returnsCorrectData() = runTest {
        setupProductAndOrder(1L, 1L, BigDecimal.TEN)
        productDao.addItemToBasket(1L, 1L, BigDecimal.TEN)

        val results =
            productDao.getProductsWithCounts(ProductType.PRODUCT, OrderStatus.ON_BASKET).first()
        assertNotNull(results.find { it.productId == 1L && it.count == 1 })
    }

    @Test
    fun incrementItemCount_updatesItemCountAndOrderTotal() = runTest {
        val pricePerItem = BigDecimal("10.00")
        setupProductAndOrder(1L, 1L, pricePerItem, 1)

        productDao.addItemToBasket(1L, 1L, pricePerItem)

        val items = productDao.getItemByProductAndOrder(1L, 1L)
        assertNotNull(items)
        assertEquals(2, items?.count)

        val order = productDao.getOrderById(1L)
        assertEquals(
            pricePerItem.multiply(BigDecimal(2)).toDouble(),
            order.totalPrice.toDouble(),
            0.05
        )
    }

    @Test
    fun decrementItemCount_decrementsItemCountAndUpdatesOrderTotal() = runTest {
        val pricePerItem = BigDecimal("10.00")
        setupProductAndOrder(1L, 1L, pricePerItem, 2)

        productDao.decrementItemCount(1L, 1L, pricePerItem)

        val items = productDao.getItemByProductAndOrder(1L, 1L)
        assertNotNull(items)
        assertEquals(1, items?.count)

        val order = productDao.getOrderById(1L)
        assertEquals(pricePerItem.toDouble(), order.totalPrice.toDouble(), 0.05)
    }

    @Test
    fun updateActiveOrderPrice_updatesPriceCorrectly() = runTest {
        val order = OrderEntity(
            id = 1L,
            orderStatus = OrderStatus.ON_BASKET,
            totalPrice = BigDecimal("100.00")
        )
        productDao.insertOrder(order)
        productDao.updateActiveOrderPrice(1L, BigDecimal("50.00"))
        val updatedOrder = productDao.getOrderById(1L)
        assertEquals(BigDecimal("150.0"), updatedOrder.totalPrice)
    }

    @Test
    fun finishOrder_finalizesOrderCorrectly() = runTest {
        val order = OrderEntity(
            id = 1L,
            orderStatus = OrderStatus.ON_BASKET,
            totalPrice = BigDecimal("100.00")
        )
        productDao.insertOrder(order)
        productDao.finishOrder(1L, OrderStatus.FINISHED)

        val finishedOrder = productDao.getOrderById(1L)
        assertEquals(OrderStatus.FINISHED, finishedOrder.orderStatus)
        assertEquals(BigDecimal.ZERO, finishedOrder.totalPrice)
    }


    @Test
    fun cleanupZeroCountItems_removesItemsWithZeroCount() = runTest {
        val pricePerItem = BigDecimal("10.00")
        setupProductAndOrder(1L, 1L, pricePerItem, 1)

        productDao.decrementItemCount(1L, 1L, pricePerItem)
        productDao.cleanupZeroCountItems(1L, 1L)

        val items = productDao.getItemByProductAndOrder(1L, 1L)
        assertNull(items)

        val order = productDao.getOrderById(1L)
        assertEquals(BigDecimal.ZERO.toDouble(), order.totalPrice.toDouble(), 0.05)
    }
}
