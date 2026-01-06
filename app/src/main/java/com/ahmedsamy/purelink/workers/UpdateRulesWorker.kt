package com.ahmedsamy.purelink.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ahmedsamy.purelink.data.RulesRepository
import com.ahmedsamy.purelink.utils.UrlCleaner

class UpdateRulesWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val repository = RulesRepository(applicationContext)
        return if (repository.fetchAndSaveRules()) {
            UrlCleaner.reloadRules(applicationContext)
            Result.success()
        } else {
            Result.retry()
        }
    }
}
