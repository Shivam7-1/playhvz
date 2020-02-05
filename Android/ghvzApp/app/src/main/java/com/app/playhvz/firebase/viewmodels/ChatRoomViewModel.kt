/*
 * Copyright 2020 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.app.playhvz.firebase.viewmodels

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.playhvz.app.HvzData
import com.app.playhvz.firebase.classmodels.ChatRoom
import com.app.playhvz.firebase.classmodels.Message
import com.app.playhvz.firebase.classmodels.Message.Companion.FIELD__TIMESTAMP
import com.app.playhvz.firebase.operations.ChatDatabaseOperations.Companion.getChatRoomDocumentReference
import com.app.playhvz.firebase.operations.ChatDatabaseOperations.Companion.getChatRoomMessagesReference
import com.app.playhvz.firebase.utils.DataConverterUtil
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ChatRoomViewModel : ViewModel() {
    companion object {
        private val TAG = ChatRoomViewModel::class.qualifiedName
    }

    private var chatRoom: HvzData<ChatRoom> = HvzData()
    private var messageList: HvzData<List<Message>> = HvzData()

    /** Listens to a player's chat room membership updates and returns a LiveData object listing
     * the ids of the chat rooms the player is currently in. */
    fun getChatRoomObserver(
        lifecycleOwner: LifecycleOwner,
        gameId: String,
        chatRoomId: String
    ): LiveData<ChatRoom> {
        val listener = getChatRoomDocumentReference(gameId, chatRoomId).addSnapshotListener(
            EventListener<DocumentSnapshot> { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "ChatRoom listen failed. ", e)
                    return@EventListener
                }
                if (snapshot == null) {
                    return@EventListener
                }
                chatRoom.value = DataConverterUtil.convertSnapshotToChatRoom(snapshot)
            })
        chatRoom.onDestroyed = {
            listener.remove()
        }
        return chatRoom
    }

    /** Listens to a chat room's messages and returns a LiveData object listing them. */
    fun getMessagesObserver(
        lifecycleOwner: LifecycleOwner,
        gameId: String,
        chatRoomId: String
    ): LiveData<List<Message>> {
        val listener = getChatRoomMessagesReference(gameId, chatRoomId).orderBy(
            FIELD__TIMESTAMP,
            Query.Direction.ASCENDING
        ).addSnapshotListener(
            EventListener<QuerySnapshot> { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Message collection listen failed. ", e)
                    return@EventListener
                }
                if (snapshot == null) {
                    return@EventListener
                }
                val updatedList: MutableList<Message> = mutableListOf()
                for (doc in snapshot.documents) {
                    updatedList.add(DataConverterUtil.convertSnapshotToMessage(doc))
                }
                messageList.value = updatedList
            })
        messageList.onDestroyed = {
            listener.remove()
        }
        return messageList
    }
}