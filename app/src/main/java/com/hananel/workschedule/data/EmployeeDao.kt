package com.hananel.workschedule.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    
    @Query("SELECT * FROM employees ORDER BY id")
    fun getAllEmployees(): Flow<List<Employee>>
    
    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getEmployeeById(id: Int): Employee?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long
    
    @Update
    suspend fun updateEmployee(employee: Employee)
    
    @Delete
    suspend fun deleteEmployee(employee: Employee)
    
    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Int)
}


