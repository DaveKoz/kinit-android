package org.kinecosystem.kinit.viewmodel.earn

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.graphics.Color
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.util.Log
import org.kinecosystem.kinit.analytics.Analytics
import org.kinecosystem.kinit.analytics.Events
import org.kinecosystem.kinit.blockchain.Wallet
import org.kinecosystem.kinit.model.TaskState
import org.kinecosystem.kinit.model.earn.*
import org.kinecosystem.kinit.model.spend.Offer
import org.kinecosystem.kinit.navigation.Navigator
import org.kinecosystem.kinit.repository.TasksRepository
import org.kinecosystem.kinit.server.OperationResultCallback
import org.kinecosystem.kinit.server.TaskService
import org.kinecosystem.kinit.util.Scheduler
import org.kinecosystem.kinit.util.TimeUtils
import org.kinecosystem.kinit.view.TabViewModel
import org.kinecosystem.kinit.viewmodel.backup.BackupAlertManager
import java.text.SimpleDateFormat
import java.util.*

private const val AVAILABILITY_DATE_FORMAT = "MMM dd"

class EarnCategoriesViewModel(val taskRepository: TasksRepository, val wallet: Wallet,
                              val taskService: TaskService, val scheduler: Scheduler, val analytics: Analytics,
                              private val navigator: Navigator, private val backupAlertManager: BackupAlertManager?) :
        TabViewModel {

    var shouldShowTask = ObservableBoolean()
    var shouldShowTaskNotAvailableYet = ObservableBoolean()
    var shouldShowNoTask = ObservableBoolean(false)

    var nextAvailableDate: ObservableField<String> = ObservableField("")
    var isAvailableTomorrow: ObservableBoolean = ObservableBoolean(false)
    var authorName = ObservableField<String>()
    var authorImageUrl = ObservableField<String?>()
    var title = ObservableField<String?>()
    var description = ObservableField<String?>()
    var kinReward = ObservableField<String>()
    var minToComplete = ObservableField<String>()
    var isQuiz = ObservableBoolean(false)

    var isTaskStarted: ObservableBoolean = taskRepository.isTaskStarted
    var balance: ObservableField<String> = wallet.balance

    private var scheduledRunnable: Runnable? = null

    init {
        refresh()
    }

    fun categories(): List<Category> {
        return listOf(Category("1", "A", "A", "aa", "", "", Color.BLACK, Task("4")),
                Category("2", "A", "B", "aa", "", "", Color.LTGRAY, Task("5")),
                Category("3", "A", "C", "aa", "", "", Color.RED, Task("6")),
                Category("4", "A", "D", "aa", "", "", Color.YELLOW),
                Category("2", "A", "E", "aa", "", "", Color.DKGRAY, Task("5")),
                Category("3", "A", "F", "aa", "", "", Color.CYAN, Task("6")),
                Category("4", "A", "G", "aa", "", "", Color.MAGENTA),
                Category("2", "A", "H", "aa", "", "", Color.CYAN, Task("5")),
                Category("3", "A", "I", "aa", "", "", Color.RED, Task("6")),
                Category("4", "A", "G", "aa", "", "", Color.GREEN),
                Category("2", "A", "K", "aa", "", "", Color.GRAY, Task("5")),
                Category("3", "A", "L", "aa", "", "", Color.BLACK, Task("6")),
                Category("4", "A", "M", "aa", "", "", Color.BLACK),
                Category("5", "A", "N", "aa", "", "", Color.BLACK, Task("9")))
    }

    fun onItemClicked(category: Category, position: Int) {
        //TODO
        navigator.navigateTo(category)
        Log.d("####", "#####click on $category")
        //analytics.logEvent(Events.Analytics.ClickOfferItemOnSpendPage(offer.provider?.name, offer.price, offersRepository.offersCount(), offer.domain, offer.id, offer.title, position, offer.type))
    }

    fun startTask() {
        taskRepository.onTaskStarted()
        val task = taskRepository.task
        val bEvent = Events.Business.EarningTaskStarted(
                task?.provider?.name,
                task?.minToComplete,
                task?.kinReward,
                task?.tagsString(),
                task?.id,
                task?.title,
                task?.type)
        analytics.logEvent(bEvent)

        val aEvent = Events.Analytics.ClickStartButtonOnTaskPage(
                isTaskStarted.get(),
                task?.provider?.name,
                task?.minToComplete,
                task?.kinReward,
                task?.tagsString(),
                task?.id,
                task?.title,
                task?.type)
        analytics.logEvent(aEvent)
        navigator.navigateTo(Navigator.Destination.TASK)
    }

    private fun refresh() {
        taskRepository.task?.let { task ->
            isQuiz.set(task.isQuiz())
            authorName.set(task.provider?.name)
            authorImageUrl.set(task.provider?.imageUrl)
            title.set(task.title)
            description.set(task.description)
            kinReward.set(if (task.isQuiz()) {
                var totalReward = 0
                (task.questions?.forEach { question ->
                    totalReward += (question.quiz_data?.reward ?: 0)
                })
                totalReward += task.kinReward ?: 0
                totalReward.toString()
            } else task.kinReward?.toString())
            minToComplete.set(convertMinToCompleteToString(task.minToComplete))
        }
        handleAvailability()
    }

    private fun handleAvailability() {
        if (scheduledRunnable != null) {
            scheduler.cancel(scheduledRunnable)
        }

        if (taskRepository.task == null) {
            shouldShowNoTask.set(true)
            shouldShowTask.set(false)
            shouldShowTaskNotAvailableYet.set(false)
            backupAlertManager?.showNagAlertIfNeeded()
        } else {
            val taskAvailable = taskRepository.isTaskAvailable()
            shouldShowTask.set(taskAvailable)
            shouldShowTaskNotAvailableYet.set(!taskAvailable)
            shouldShowNoTask.set(false)
            if (!taskAvailable) {
                backupAlertManager?.showNagAlertIfNeeded()
            }

            if (!shouldShowTask.get()) {
                nextAvailableDate.set(nextAvailableDate())
                isAvailableTomorrow.set(isAvailableTomorrow())
                if (isAvailableTomorrow.get()) {
                    val diff = taskRepository.task?.startDateInMillis()!! - scheduler.currentTimeMillis()
                    scheduledRunnable = Runnable {
                        shouldShowTask.set(true)
                        shouldShowTaskNotAvailableYet.set(false)
                    }
                    scheduler.scheduleOnMain(scheduledRunnable, diff)
                } else {
                    scheduledRunnable = Runnable { handleAvailability() }
                    scheduler.scheduleOnMain(scheduledRunnable, DAY_IN_MILLIS)
                }
            }
        }
    }

    private fun isAvailableTomorrow(): Boolean {
        return timeToUnlockInDays(taskRepository.task) == 1
    }

    private fun timeToUnlockInDays(task: Task?): Int {
        val millisAtNextMidnight = TimeUtils.millisAtNextMidnight(scheduler.currentTimeMillis())
        val startDate = task?.startDateInMillis() ?: scheduler.currentTimeMillis()

        return (1 + ((startDate - millisAtNextMidnight) / DAY_IN_MILLIS)).toInt()
    }

    private fun nextAvailableDate(): String {
        taskRepository.task?.startDateInMillis()?.let {
            return SimpleDateFormat(AVAILABILITY_DATE_FORMAT, Locale.US).format(Date(it))
        }
        return ""
    }

    override fun onScreenVisibleToUser() {
        refresh()
        when {
            shouldShowTask.get() -> onEarnScreenVisible()
            shouldShowNoTask.get() -> onNoTasksAvailableVisible()
            else -> onLockedScreenVisible()
        }
        checkForUpdates()
    }

    private fun checkForUpdates() {
        taskService.retrieveNextTask(object : OperationResultCallback<Boolean> {
            override fun onError(errorCode: Int) {
            }

            override fun onResult(taskHasChanged: Boolean) {
                if (taskHasChanged) {
                    refresh()
                }
            }
        })
    }

    private fun onEarnScreenVisible() {
        val task = taskRepository.task
        val event = Events.Analytics.ViewTaskPage(task?.provider?.name,
                task?.minToComplete,
                task?.kinReward,
                task?.tagsString(),
                task?.id,
                task?.title,
                task?.type)
        analytics.logEvent(event)
    }

    private fun onLockedScreenVisible() {
        val task = taskRepository.task
        val timeToUnlockInDays = timeToUnlockInDays(task)

        val event = Events.Analytics.ViewLockedTaskPage(timeToUnlockInDays)
        analytics.logEvent(event)
    }

    private fun onNoTasksAvailableVisible() {
        analytics.logEvent(Events.Analytics.ViewEmptyStatePage(Analytics.MENU_ITEM_NAME_EARN))
    }

    private fun convertMinToCompleteToString(minToComplete: Float?): String =
            when {
                minToComplete == null -> "0"
                (minToComplete * 10).toInt() % 10 == 0 -> minToComplete.toInt().toString()
                else -> minToComplete.toString()
            }
}
