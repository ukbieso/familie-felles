package no.nav.familie.prosessering.internal

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.familie.prosessering.TestAppConfig
import no.nav.familie.prosessering.domene.Status
import no.nav.familie.prosessering.domene.Task.Companion.nyTask
import no.nav.familie.prosessering.domene.TaskRepository
import no.nav.familie.prosessering.task.TaskStep1
import no.nav.familie.prosessering.task.TaskStep2
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [TestAppConfig::class])
@DataJpaTest(excludeAutoConfiguration = [FlywayAutoConfiguration::class])
class TaskWorkerTest {

    @MockkBean(relaxUnitFun = true)
    lateinit var task: TaskStep2

    @Autowired
    private lateinit var repository: TaskRepository

    @Autowired
    private lateinit var worker: TaskWorker

    @Test
    fun `skal behandle task`() {
        var task1 =
                nyTask(TaskStep1.TASK_1, "{'a'='b'}")
        repository.saveAndFlush(task1)
        assertThat(task1.status)
                .isEqualTo(Status.UBEHANDLET)

        worker.doActualWork(task1.id!!)

        task1 = repository.findById(task1.id!!).orElseThrow()
        assertThat(task1.status).isEqualTo(Status.FERDIG)
        assertThat(task1.logg).hasSize(3)
    }

    @Test
    fun `skal håndtere feil`() {
        var task2 = nyTask(TaskStep2.TASK_2, "{'a'='b'}")
        repository.saveAndFlush(task2)
        assertThat(task2.status).isEqualTo(Status.UBEHANDLET)
        every { task.doTask(any()) } throws (IllegalStateException())

        worker.doActualWork(task2.id!!)
        task2 = repository.findById(task2.id!!).orElseThrow()
        assertThat(task2.status).isEqualTo(Status.KLAR_TIL_PLUKK)
        assertThat(task2.logg).hasSize(3)

        worker.doActualWork(task2.id!!)
        task2 = repository.findById(task2.id!!).orElseThrow()
        assertThat(task2.status).isEqualTo(Status.KLAR_TIL_PLUKK)

        worker.doActualWork(task2.id!!)
        task2 = repository.findById(task2.id!!).orElseThrow()
        assertThat(task2.status).isEqualTo(Status.FEILET)
    }
}
