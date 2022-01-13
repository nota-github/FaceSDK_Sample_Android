package com.nota.hyundai_lobby.data

data class User(val id: String = "", val name: String, val dong: String, val ho: String){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        if (id != other.id) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + dong.hashCode()
        result = 31 * result + ho.hashCode()
        return result
    }

}
