package seg3502.lab6

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
class CalculatorApiTests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @ParameterizedTest
    @CsvSource(
        "add, 2, 3, 5.0",
        "add, 0, 0, 0.0",
        "subtract, 2, 3, -1.0",
        "multiply, -2, 3.5, -7.0",
        "divide, 10, 4, 2.5",
        "divide, 0, 3, 0.0"
    )
    fun `operations return numeric results`(operation: String, first: String, second: String, expected: String) {
        mockMvc.get("/calculator/$operation/$first/$second").andExpect {
            status { isOk() }
            content {
                contentTypeCompatibleWith("application/json")
                string(expected)
            }
        }
    }

    @ParameterizedTest
    @CsvSource("add/abc/2", "subtract/2/abc", "multiply/abc/2", "divide/10/0", "divide/10/-0.0")
    fun `invalid requests return bad request`(path: String) {
        mockMvc.get("/calculator/$path").andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `OpenAPI documents every calculator operation`() {
        mockMvc.get("/v3/api-docs").andExpect {
            status { isOk() }
            jsonPath("$.openapi") { exists() }
            for (operation in listOf("add", "subtract", "multiply", "divide")) {
                jsonPath("$.paths['/calculator/$operation/{nombre1}/{nombre2}'].get") { exists() }
            }
        }
        mockMvc.get("/v3/api-docs.yaml").andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `Swagger UI is available`() {
        mockMvc.get("/swagger-ui.html").andExpect {
            status { is3xxRedirection() }
            redirectedUrl("/swagger-ui/index.html")
        }
        mockMvc.get("/swagger-ui/index.html").andExpect {
            status { isOk() }
        }
    }
}
