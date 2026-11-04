package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.student.dataanalysis.expense.entity.Report;
import ru.student.dataanalysis.expense.entity.ReportStatus;
import ru.student.dataanalysis.expense.repository.ReportRepository;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ReportRepository reportRepository;

    @Test
    void testCreateReportReturnsIdAndReportBecomesAvailable() throws Exception {
        String id = mockMvc.perform(post("/custom/reports"))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                mockMvc.perform(get("/custom/reports/" + id).with(user("tester")))
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                        .andExpect(content().string(containsString("<table")))
                        .andExpect(content().string(containsString("Общее время формирования отчёта"))));
    }

    @Test
    void testReportNotReadyYet() throws Exception {
        Report report = reportRepository.save(new Report());

        mockMvc.perform(get("/custom/reports/" + report.getId()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Отчёт ещё формируется"));
    }

    @Test
    void testReportFailed() throws Exception {
        Report report = new Report();
        report.setStatus(ReportStatus.ERROR);
        report.setContent("сломалось");
        report = reportRepository.save(report);

        mockMvc.perform(get("/custom/reports/" + report.getId()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Отчёт завершился с ошибкой: сломалось"));
    }

    @Test
    void testUnknownReportIsNotFound() throws Exception {
        mockMvc.perform(get("/custom/reports/-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @org.springframework.security.test.context.support.WithAnonymousUser
    void testAnonymousRedirectedToLogin() throws Exception {
        mockMvc.perform(post("/custom/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        mockMvc.perform(get("/custom/reports/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
