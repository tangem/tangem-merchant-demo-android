package com.tangem.merchant.application.domain.store

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

/**
 * Created by Anton Zhilenkov on 26/06/2020.
 */
interface Store<M> {
    fun save(value: M)
    fun restore(): M
    fun has():Boolean
}

abstract class BaseStore<M>(
    protected val sp: SharedPreferences,
    private val key: String,
    protected val gson: Gson = Gson()
) : Store<M> {

    override fun save(value: M) {
        sp.edit(true) { putString(key, toJson(value)) }
    }

    override fun restore(): M {
        val json = sp.getString(key, toJson(getDefault()))
        return fromJson(json!!)
    }

    override fun has(): Boolean = sp.contains(key)

    protected open fun toJson(value: M): String = gson.toJson(value)

    protected abstract fun getDefault(): M
    protected abstract fun fromJson(json: String): M

}