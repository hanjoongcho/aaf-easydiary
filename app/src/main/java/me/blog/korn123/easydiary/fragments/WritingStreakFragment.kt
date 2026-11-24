package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.blog.korn123.easydiary.databinding.FragmentWritingStreakBinding
import me.blog.korn123.easydiary.extensions.startDiaryReadingActivity
import me.blog.korn123.easydiary.presentation.streak.WritingStreakViewModel
import me.blog.korn123.easydiary.presentation.streak.components.WritingStreakCard
import me.blog.korn123.easydiary.presentation.theme.AppTheme

/**
 * The writing streak card of the dashboard (issue #88).
 *
 * The card itself is Compose, as new UI in this app is, and the fragment only holds it inside the card the
 * dashboard lays out, next to the other cards. [onResume] tells the view model to count again from today, so a
 * dashboard left open overnight does not keep showing yesterday's state.
 *
 * A tap on a square of the card's grid hands the entry it leads to over here, and the entry is opened the way
 * the rest of the app opens one: the reading screen of [startDiaryReadingActivity].
 */
@AndroidEntryPoint
class WritingStreakFragment : Fragment() {
    private lateinit var binding: FragmentWritingStreakBinding
    private val writingStreakViewModel: WritingStreakViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWritingStreakBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        binding.composeView.setContent {
            AppTheme {
                val uiState by writingStreakViewModel.uiState.collectAsStateWithLifecycle()
                WritingStreakCard(
                    uiState = uiState,
                    onRuleSelected = writingStreakViewModel::setTwoDayRule,
                    onDiarySelected = { diaryId -> requireContext().startDiaryReadingActivity(diaryId) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        writingStreakViewModel.refreshToday()
    }
}
