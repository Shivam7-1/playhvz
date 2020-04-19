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

package com.app.playhvz.screens.rules

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.playhvz.R
import com.app.playhvz.app.EspressoIdlingResource
import com.app.playhvz.common.globals.SharedPreferencesConstants
import com.app.playhvz.firebase.classmodels.Game
import com.app.playhvz.firebase.operations.GameDatabaseOperations
import com.app.playhvz.firebase.viewmodels.GameViewModel
import com.app.playhvz.navigation.NavigationUtil
import com.app.playhvz.utils.GameUtils
import com.app.playhvz.utils.SystemUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.runBlocking


class RulesFragment : Fragment() {

    private var gameId: String? = null
    private var game: Game? = null
    private var currentCollapsibleSections: MutableList<Game.CollapsibleSection> = mutableListOf()
    private var isEditing: Boolean = false
    private var isSaving: Boolean = false
    private var editAdapter: EditCollapsibleSectionAdapter? = null

    private lateinit var fab: FloatingActionButton
    private lateinit var gameViewModel: GameViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var toolbar: ActionBar
    private lateinit var toolbarMenu: Menu
    private lateinit var displayAdapter: DisplayCollapsibleSectionAdapter
    private lateinit var rulesList: RecyclerView
    private lateinit var errorLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar = (activity as AppCompatActivity).supportActionBar!!
        gameViewModel = GameViewModel()
        displayAdapter = DisplayCollapsibleSectionAdapter(listOf(), requireContext())

        val sharedPrefs = activity?.getSharedPreferences(
            SharedPreferencesConstants.PREFS_FILENAME,
            0
        )!!
        gameId = sharedPrefs.getString(SharedPreferencesConstants.CURRENT_GAME_ID, null)
        if (gameId == null) {
            NavigationUtil.navigateToGameList(findNavController(), requireActivity())
        }

        setupToolbar()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_rules, container, false)
        fab = requireActivity().findViewById(R.id.floating_action_button)
        progressBar = view.findViewById(R.id.progress_bar)
        errorLabel = view.findViewById(R.id.error_label)
        rulesList = view.findViewById(R.id.collapsible_item_list)
        rulesList.layoutManager = LinearLayoutManager(context)
        rulesList.adapter = displayAdapter

        setupObservers()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save_settings, menu)
        toolbarMenu = menu
        setupFab()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save_option -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupToolbar() {
        toolbar.title = getString(R.string.navigation_drawer_rules)
        toolbar.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(true)
    }

    private fun setupObservers() {
        if (gameId.isNullOrEmpty()) {
            return
        }

        val onFailure = {
            NavigationUtil.navigateToGameList(findNavController(), requireActivity())
        }
        gameViewModel.getGame(gameId!!, onFailure)
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer { serverGame: Game ->
                updateGame(serverGame)
            })
    }

    private fun setupFab() {
        fab.visibility = View.GONE
        fab.setOnClickListener {
            if (isEditing) {
                exitEditMode()
            } else {
                enterEditMode()
            }
        }
        exitEditMode()
    }

    private fun hideProgressBar() {
        if (this.view == null) {
            return
        }
        progressBar.visibility = View.GONE
    }

    private fun hideActionBarActions() {
        val menuItem = toolbarMenu.findItem(R.id.save_option)
        menuItem.isVisible = false
    }

    private fun showActionBarActions() {
        val menuItem = toolbarMenu.findItem(R.id.save_option)
        menuItem.isVisible = true
    }

    private fun enterEditMode() {
        if (isEditing) {
            return
        }
        isEditing = true
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_x))
        showActionBarActions()
        currentCollapsibleSections = game!!.rules.toMutableList()
        editAdapter?.setData(game!!.rules)
        rulesList.adapter = editAdapter
    }

    private fun exitEditMode() {
        hideActionBarActions()
        fab.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit))
        isEditing = false
        rulesList.adapter = displayAdapter
        updateUi()
    }

    private fun updateGame(serverGame: Game) {
        hideProgressBar()
        game = serverGame
        if (isSaving) {
            return
        }
        if (isEditing) {
            errorLabel.visibility = View.VISIBLE
            toolbarMenu.findItem(R.id.save_option).isEnabled = false
            return
        } else if (errorLabel.visibility == View.VISIBLE) {
            errorLabel.visibility = View.GONE
        }
        updateUi()
    }

    private fun updateUi() {
        if (game == null) {
            displayAdapter.setData(listOf())
            displayAdapter.notifyDataSetChanged()
            return
        }
        if (GameUtils.isAdmin(game!!)) {
            fab.visibility = View.VISIBLE
            val onRuleAdded = {
                val newRule = Game.CollapsibleSection()
                newRule.order = currentCollapsibleSections.size
                currentCollapsibleSections.add(newRule)
                editAdapter?.setData(currentCollapsibleSections)
                editAdapter?.notifyDataSetChanged()
            }
            if (editAdapter == null) {
                editAdapter = EditCollapsibleSectionAdapter(listOf(), this, onRuleAdded)

            }
        } else if (fab.visibility == View.VISIBLE) {
            fab.visibility = View.GONE
        }
        displayAdapter.setData(game!!.rules)
        displayAdapter.notifyDataSetChanged()
    }

    private fun saveChanges() {
        if (game == null) {
            return
        }
        isSaving = true
        disableActions()

        game?.rules = editAdapter!!.getLatestData()
        val onSuccess = {
            isSaving = false
            exitEditMode()
            enableActions()
            updateUi()
        }
        runBlocking {
            EspressoIdlingResource.increment()
            GameDatabaseOperations.asyncUpdateGame(
                game!!,
                onSuccess,
                {
                    isSaving = false
                    enableActions()
                    SystemUtils.showToast(context, "Couldn't save rules.")
                }
            )
            EspressoIdlingResource.decrement()
        }
    }

    private fun disableActions() {
        SystemUtils.hideKeyboard(requireContext())
        progressBar.visibility = View.VISIBLE
        val menuItem = toolbarMenu.findItem(R.id.save_option)
        menuItem.icon.mutate().alpha = 130
        menuItem.isEnabled = false
        fab.isEnabled = false
    }

    private fun enableActions() {
        progressBar.visibility = View.GONE
        val menuItem = toolbarMenu.findItem(R.id.save_option)
        menuItem.icon.mutate().alpha = 255
        menuItem.isEnabled = true
        fab.isEnabled = true
    }
}