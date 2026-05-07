package com.example.grabthisforme.model.user.data.repository

import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository@Inject constructor(
    private val userDao: UserDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUser: StateFlow<User?> = userDao.getCurrentUser()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val currentUserId: StateFlow<Long?> = currentUser
        .map { it?.id }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val allLoginUsers: StateFlow<List<User>> = userDao.getAllLoginUsers()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
}
