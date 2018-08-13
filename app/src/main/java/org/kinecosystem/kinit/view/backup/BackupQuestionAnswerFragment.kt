package org.kinecosystem.kinit.view.backup

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.backup_question_answer_layout.*
import org.kinecosystem.kinit.R
import org.kinecosystem.kinit.databinding.BackupQuestionAnswerLayoutBinding
import org.kinecosystem.kinit.util.GeneralUtils
import org.kinecosystem.kinit.view.BaseFragment
import org.kinecosystem.kinit.viewmodel.Backup.BackUpModel


class BackupQuestionAnswerFragment : BaseFragment() {
    companion object {
        fun newInstance(): Fragment = BackupQuestionAnswerFragment()
    }

    lateinit var model: BackUpModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questions.adapter = BackupQuestionSpinnerAdapter(activity?.applicationContext, model.getHints())
        questions.setOnTouchListener { _, _ ->
            GeneralUtils.closeKeyboard(activity, questions)
            false
        }
        //item selected listener for spinner
        questions.setSelection(0, false)

        next.setOnClickListener {
            activity?.let {
                (it as BackUpActions).onNext()
            }
        }

        backBtn.setOnClickListener({
            activity?.let {
                (it as BackUpActions).onBack()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (activity !is BackUpActions) {
            Log.e("BackupQAFragment", "Activity must implement BackUpActions")
            activity?.finish()
        }
        model = (activity as BackUpActions).getBackUpModel()
        val binding = DataBindingUtil.inflate<BackupQuestionAnswerLayoutBinding>(inflater, R.layout.backup_question_answer_layout, container, false)
        binding.model = model
        return binding.root
    }
}