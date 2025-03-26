package com.proyek.foolens.data.util

import com.proyek.foolens.data.remote.dto.UserDto
import com.proyek.foolens.domain.model.User

/**
 * DataMapper bertugas melakukan transformasi data antara model domain dan DTO.
 * Object ini digunakan oleh Repository untuk mengkonversi antara model data dan domain.
 */
object DataMapper {
    /**
     * Mengkonversi UserDto (dari API) ke User domain model
     *
     * @param dto UserDto dari API
     * @return User domain model untuk digunakan di aplikasi
     */
    fun mapUserDtoToDomain(dto: UserDto): User {
        return User(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            token = dto.token
        )
    }

    /**
     * Mengkonversi User domain model ke UserDto untuk dikirim ke API
     * (Tidak digunakan dalam implementasi saat ini tetapi bisa berguna untuk masa depan)
     *
     * @param domain User domain model
     * @return UserDto untuk dikirim ke API
     */
    fun mapUserDomainToDto(domain: User): UserDto {
        return UserDto(
            id = domain.id,
            name = domain.name,
            email = domain.email,
            token = domain.token
        )
    }
}