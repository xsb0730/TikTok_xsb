package com.example.tiltok_xsb.utils

import io.reactivex.subjects.Subject
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class RxBus private constructor(){
    val bus:Subject<Any> = PublishSubject.create<Any>().toSerialized()

    companion object{
        @Volatile
        private var instance:RxBus? = null

        @JvmStatic
        fun getDefault():RxBus{
            return instance?: synchronized(this){
                instance?:RxBus().also{instance=it}
            }
        }
    }

    fun post(event:Any){
        bus.onNext(event)
    }

    fun <T> toObservable(eventType:Class<T>):Observable<T>{
        return bus.ofType(eventType)
    }

    inline fun <reified T> toObservable():Observable<T>{
        return bus.ofType(T::class.java)
    }
}