package com.yoshua.vesti.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yoshua.vesti.repository.models.Probador
import com.yoshua.vesti.repository.models.TipoUsuario
import com.yoshua.vesti.repository.models.User

@Database(
    entities = [Probador::class, User::class, TipoUsuario::class],
    version = 4, // v2: se agrega tipo_usuarios y FK en users
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun probadorDao(): ProbadorDao
    abstract fun userDao(): UserDao
    abstract fun tipoUsuarioDao(): TipoUsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // Proyecto académico: al cambiar la versión se recrea la BD
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Datos iniciales. Se ejecuta en cada apertura con INSERT OR IGNORE,
        // así también se cargan después de una migración destructiva.
        private val seedCallback = object : Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL(
                    "INSERT OR IGNORE INTO tipo_usuarios (id, tipo_usuario) VALUES " +
                        "(1, 'Administrador'), (2, 'Agente'), (3, 'Supervisor')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO users (name, email, password, profileImageUri, tipoUsuarioId) " +
                        "VALUES ('Administrador', 'admin@duoc.cl', 'admin123', NULL, 1)"
                )
            }
        }
    }
}
