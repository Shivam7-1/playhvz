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

package com.app.playhvz.screens.missions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.playhvz.R
import com.app.playhvz.common.globals.SharedPreferencesConstants
import com.app.playhvz.firebase.classmodels.Game
import com.app.playhvz.firebase.classmodels.Player
import com.app.playhvz.firebase.viewmodels.GameViewModel
import com.app.playhvz.navigation.NavigationUtil
import com.app.playhvz.utils.GameUtils
import com.app.playhvz.utils.PlayerUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

/** Fragment for showing a list of missions.*/
class MissionDashboardFragment : Fragment() {
    companion object {
        private val TAG = MissionDashboardFragment::class.qualifiedName
    }

    lateinit var gameViewModel: GameViewModel
    lateinit var fab: FloatingActionButton
    lateinit var recyclerView: RecyclerView

    var gameId: String? = null
    var playerId: String? = null
    var game: Game? = null
    var player: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = activity?.getSharedPreferences(
            SharedPreferencesConstants.PREFS_FILENAME,
            0
        )!!
        gameViewModel = GameViewModel()
        gameId = sharedPrefs.getString(SharedPreferencesConstants.CURRENT_GAME_ID, null)
        playerId = sharedPrefs.getString(SharedPreferencesConstants.CURRENT_PLAYER_ID, null)
        setupObservers()
        setupToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mission_dashboard, container, false)
        fab = activity?.findViewById(R.id.floating_action_button)!!
        recyclerView = view.findViewById(R.id.mission_list)
        val adapter = MissionDashboardAdapter(listOf<String>(), context!!)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        return view
    }

    fun setupToolbar() {
        val toolbar = (activity as AppCompatActivity).supportActionBar
        if (toolbar != null) {
            toolbar.title = context!!.getString(R.string.mission_title)
            toolbar.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setupFab(isAdmin: Boolean) {
        if (!isAdmin) {
            fab.visibility = View.GONE
            return
        }
        fab.visibility = View.VISIBLE
        fab.setOnClickListener {
            createMission()
        }
        fab.visibility = View.VISIBLE
    }

    private fun setupObservers() {
        if (gameId == null || playerId == null) {
            return
        }
        gameViewModel.getGame(gameId!!)
            .observe(this, androidx.lifecycle.Observer { serverGame ->
                updateGame(serverGame)
            })
        PlayerUtils.getPlayer(gameId!!, playerId!!)
            .observe(this, androidx.lifecycle.Observer { serverPlayer ->
                updatePlayer(serverPlayer)
            })
    }

    private fun updateGame(serverGame: Game?) {
        game = serverGame
        setupFab(GameUtils.isAdmin(game!!))
    }

    private fun updatePlayer(serverPlayer: Player?) {
        if (serverPlayer == null) {
            NavigationUtil.navigateToGameList(findNavController(), activity!!)
        }
    }

    private fun createMission() {
        NavigationUtil.navigateToMissionSettings(findNavController(), null)
    }
}