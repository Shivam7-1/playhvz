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

package com.app.playhvz.firebase.constants

import com.app.playhvz.firebase.firebaseprovider.FirebaseProvider

class MissionPath {
    companion object {
        /**
         * Top level collection name for Missions.
         */
        const val MISSION_COLLECTION_PATH = "missions"

        /*******************************************************************************************
         * Begin string definitions for field names in Firebase documents. Alphabetize.
         ******************************************************************************************/
        /**
         * Field inside Mission document that contains the mission details.
         */
        const val CHAT_FIELD__DETAILS = "details"

        /**
         * Field inside Mission document that contains the mission ending time.
         */
        const val CHAT_FIELD__END_TIME = "endTime"

        /**
         * Field inside Mission document that contains the group id associated with the mission.
         */
        const val CHAT_FIELD__GROUP_ID = "associatedGroupId"

        /**
         * Field inside Mission document that contains the mission name.
         */
        const val CHAT_FIELD__NAME = "name"

        /**
         * Field inside Mission document that contains the mission starting time.
         */
        const val CHAT_FIELD__START_TIME = "startTime"

        /*******************************************************************************************
         * End string definitions for field names in Firebase documents.
         ******************************************************************************************/


        /*******************************************************************************************
         * Begin path definitions to documents. Alphabetize.
         ******************************************************************************************/

        /**
         * DocRef that navigates to Mission documents.
         */
        val MISSION_COLLECTION = { gameId: String ->
            GamePath.GAMES_COLLECTION.document(gameId).collection(MISSION_COLLECTION_PATH)
        }

        val MISSION_DOCUMENT_REFERENCE = { gameId: String, missionId: String ->
            MISSION_COLLECTION(gameId).document(missionId)
        }

        /*******************************************************************************************
         * End path definitions to documents
         ******************************************************************************************/
    }
}