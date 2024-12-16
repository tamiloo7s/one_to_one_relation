package com.sky.relation_1_to_1

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import com.sky.relation_1_to_1.ui.theme.Relation_1_to_1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Relation_1_to_1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   Column(modifier = Modifier
                       .fillMaxSize()
                       .padding(innerPadding))
                   {
                       OneToOneDb(this@MainActivity)
                   }
                }
            }
        }
    }
}



@Entity(tableName = "owner")
data class Owner(
    @PrimaryKey
    val id: Int,
    val name: String)

@Entity(tableName = "dog")
data class Dog(
    @PrimaryKey
    val id: Int,
    val name: String,
    val owner_id: Int
)

data class OwnerandDog(
    @Embedded var owner:Owner,
    @Relation(
        parentColumn = "id",
        entityColumn = "owner_id"
    )
    var dog: Dog
)



@Dao
interface userdao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert_owner(owner: Owner)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert_dog(dog:Dog)

    @Transaction
    @Query("SELECT * FROM Owner WHERE id = :id")
    fun getOwnerAndDog(id: Int): LiveData<OwnerandDog>

}





@Database(entities = [Owner::class, Dog::class], version = 1, exportSchema = false)
abstract class RoomDB:RoomDatabase(){
    abstract fun userdao():userdao

    companion object{
        private var INSTANCE:RoomDB?=null

        fun getinstance(context:Context):RoomDB{
            synchronized(this){
                return INSTANCE?:Room.databaseBuilder(
                    context.applicationContext,
                    RoomDB::class.java,
                    "database"
                ).allowMainThreadQueries().build().apply {
                    INSTANCE = this
                }
            }
        }
    }
}

class rep(val dao:userdao){

    fun insert_owner(owner:Owner){
        dao.insert_owner(owner)
    }

    fun insert_dog(dog: Dog){
        dao.insert_dog(dog)
    }

    fun getOwnerAndDog(id: Int):LiveData<OwnerandDog>{
        return dao.getOwnerAndDog(id)
    }

}

class vm(app:Application):AndroidViewModel(app){
    var obj :rep?=null

    init{
        var instance = RoomDB.getinstance(app).userdao()
        obj = rep(instance)
    }

    fun insert_owner(owner: Owner){
        obj!!.insert_owner(owner)
    }

    fun insert_dog(dog: Dog){
        obj!!.insert_dog(dog)
    }

    fun getOwnerAndDog(id:Int):LiveData<OwnerandDog>{
        return obj!!.getOwnerAndDog(id)
    }

}



val owner = listOf(
    Owner(1,"danish"),
    Owner(2,"lala"),
    Owner(3,"kumar"),
    Owner(4,"raja"),
    Owner(5,"guru")
)

val dog = listOf(
    Dog(1,"Rocky",5),
    Dog(2,"Juily",4),
    Dog(3,"Sweety",3),
    Dog(4,"Tom",2),
    Dog(5,"Oscar",1)
)


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "SuspiciousIndentation")
@Composable
fun OneToOneDb(mainActivity: MainActivity) {

    var vm  = ViewModelProvider(mainActivity)[vm::class.java]

    var ownerId by remember { mutableStateOf("1") }


    var ownerAndDog =
        vm.getOwnerAndDog(if (ownerId.isNotBlank() && ownerId.isDigitsOnly()) ownerId.toInt() else -1)
            .observeAsState()


                owner.forEach {
                    vm.insert_owner(it)
                }

                dog.forEach {
                    vm.insert_dog(it)
                }




    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            Text("One to One Relationship")

            Spacer(modifier = Modifier.height(50.dp))

            OutlinedTextField(
                value = ownerId,
                onValueChange = { ownerId = it },
                label = { Text(text = "Enter Owner Id") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Cyan)
                    .padding(15.dp)
            ) {
                Text(
                    text = "Owner Id",
                    modifier = Modifier.weight(0.3f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Owner Name",
                    modifier = Modifier.weight(0.3f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Dog Name",
                    modifier = Modifier.weight(0.3f),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ownerAndDog.value?.owner?.id != null && ownerAndDog.value?.owner?.name != null && ownerAndDog.value?.dog?.name != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(15.dp)
                ) {
                    Text(
                        text = ownerAndDog.value!!.owner.id.toString(),
                        modifier = Modifier.weight(0.3f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ownerAndDog.value!!.owner.name,
                        modifier = Modifier.weight(0.3f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = ownerAndDog.value!!.dog.name,
                        modifier = Modifier.weight(0.3f),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}