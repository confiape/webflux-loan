package loan.infraestructure.midelware.error

import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.*
import reactor.core.publisher.Mono


@Component
class ReactiveExceptionHandler(
    errorAttributes: ErrorAttributes,
    webProperties: WebProperties,
    applicationContext: ApplicationContext,
    codecConfigurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, webProperties.resources, applicationContext) {

    init {
        super.setMessageWriters(codecConfigurer.writers)
        super.setMessageReaders(codecConfigurer.readers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(RequestPredicates.all()) { request: ServerRequest ->
            this.renderErrorResponse(request)
        }
    }

    private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val error = getError(request)

        val httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

        val errorResponse = loan.dtos.response.ErrorResponse(
            message = error.message ?: "Ocurrió un error inesperado"
        )

        return ServerResponse
            .status(httpStatus)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorResponse))
    }

}
