package com.smartshop.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smartshop.app.data.repository.CartRepository
import com.smartshop.app.data.repository.OrderRepository
import com.smartshop.app.data.repository.ProductRepository
import com.smartshop.app.utils.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent:: class)
object AppModule{
    // Tell Hilt how to create FirebaseAuth
    //@Singleton means only ONE instance exists for the whole app
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(
        @ApplicationContext context: Context
    ): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        firestore: FirebaseFirestore
    ): ProductRepository {
        return ProductRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideCartRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): CartRepository {
        return CartRepository(firestore, auth)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): OrderRepository {
        return OrderRepository(firestore, auth)
    }
}