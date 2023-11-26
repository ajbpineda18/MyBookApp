package com.example.mybookapp.activities.realm

import com.example.mybookapp.activities.models.Books
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class RealmDatabase {
    private val realm: Realm by lazy {
        val config =
            RealmConfiguration.Builder(setOf(BookRealm::class)).schemaVersion(1).initialData {

            }
                .build()
        Realm.open(config)
    }
    fun getAllBooks(): List<BookRealm>{
        return realm.query<BookRealm>("isArchived == false && isFav == false").find()
    }
    fun getArchivedBooks(): List<BookRealm>{
        return realm.query<BookRealm>("isArchived == true").find()
    }
    fun getFavoriteBooks(): List<BookRealm>{
        return realm.query<BookRealm>("isFav == true").find()
    }
    suspend fun addBook(bookName: String, bookAuthor: String, datePublished: Long, dateAdded: Long, dateModified: Long) {
        val dupeBookChecker: BookRealm? = realm.query<BookRealm>("author == $0 && name == $1 && dateBookPublished == $2", bookAuthor, bookName, datePublished).first().find()
        realm.write {
            if(dupeBookChecker == null){
                val newBook = BookRealm().apply{
                    name = bookName
                    author = bookAuthor
                    dateBookPublished = datePublished
                    dateBookAdded = dateAdded
                    dateBookModified = dateModified
                }

                val manageBook = copyToRealm(newBook)
            }
            else{
                throw IllegalStateException("Book Duplicate!.")
            }
        }
    }
    suspend fun favBook(book: Books){
        realm.write {
            val bookID = BsonObjectId(book.id)
            val bookRealm = query<BookRealm>("id == $0", bookID).first().find()
            if(bookRealm != null){
                findLatest(bookRealm).apply {
                    this!!.isFav = true
                }
            }
            else{
                throw IllegalStateException("Book with Title: $bookID is Not Found. Cannot Update.")
            }
        }
    }
    suspend fun unFavBook(book: Books){
        realm.write {
            val bookID = BsonObjectId(book.id)
            val bookRealm = query<BookRealm>("id == $0", bookID).first().find()
            if(bookRealm != null){
                findLatest(bookRealm).apply {
                    this!!.isFav = false
                }
            }
            else{
                throw IllegalStateException("Book with Title: $bookID is Not Found. Cannot Update.")
            }
        }
    }
    suspend fun archiveBook(book: Books){
        val archiveBookID = BsonObjectId(book.id)
        val bookRealm = realm.query<BookRealm>("id == $0", archiveBookID).first().find()
        realm.write {
            if(bookRealm != null){
                findLatest(bookRealm).apply {
                    this!!.isArchived = true
                }
            }
            else{
                throw IllegalStateException("Book with Title: $archiveBookID is Not Found. Cannot Update.")
            }
        }
    }
    suspend fun unArchiveBook(book: Books){
        val archiveBookID = BsonObjectId(book.id)
        val bookRealm = realm.query<BookRealm>("id == $0", archiveBookID).first().find()
        realm.write {
            if(bookRealm != null){
                findLatest(bookRealm).apply {
                    this!!.isArchived = false
                }
            }
            else{
                throw IllegalStateException("Book with Title: $archiveBookID is Not Found. Cannot Update.")
            }
        }
    }
    suspend fun deleteBook(bookId: ObjectId){
        realm.write {
            query<BookRealm>("id == $0", bookId).first().find()?.let { delete(it) } ?: throw IllegalStateException("Book Not Found")
        }
    }
}