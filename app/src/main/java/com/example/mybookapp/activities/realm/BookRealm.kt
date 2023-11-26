package com.example.mybookapp.activities.realm

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

class BookRealm : RealmObject{
    @PrimaryKey
    var id: ObjectId = ObjectId()
    var author: String = ""
    var name: String = ""
    var dateBookPublished: Long = 0
    var dateBookAdded: Long = 0
    var dateBookModified: Long = 0
    var isFav: Boolean = false
    var isArchived: Boolean = false
}