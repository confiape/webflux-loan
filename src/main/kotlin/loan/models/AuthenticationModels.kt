package loan.models

import org.bson.types.ObjectId

data class User(
    var id: String = ObjectId().toString(),
    var username: String,
    var password: String
)
