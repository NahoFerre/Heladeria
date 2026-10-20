package com.heladeria.backend.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, String>> manejarStockInsuficiente(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CajaException.class)
    public ResponseEntity<Map<String, String>> manejarCaja(CajaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ConfiguracionProductoException.class)
    public ResponseEntity<Map<String, String>> manejarConfiguracionProducto(ConfiguracionProductoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(VentaException.class)
    public ResponseEntity<Map<String, String>> manejarVenta(VentaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    // @Valid en un @RequestBody (ej. falta un campo obligatorio): esto ya lo manejaba Spring por su cuenta
    // con 400, pero el catch-all de más abajo lo hubiera convertido en 500 si no estuviera este handler
    // específico antes (Spring elige siempre el handler más específico, así que el orden en el archivo no
    // importa, pero si faltara este método sí se perdería el 400).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", mensaje.isBlank() ? "Datos inválidos" : mensaje));
    }

    // JSON mal formado o body vacío
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "El cuerpo de la solicitud no es válido"));
    }

    // Cualquier error que no sea uno de los de arriba (que son reglas de negocio esperadas) es un bug real:
    // se registra completo (mensaje + stack trace, con el archivo y la línea exacta donde ocurrió) en el
    // log para poder diagnosticarlo después. Al cliente nunca se le devuelve el detalle interno.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarError(Exception ex) {
        log.error("Error inesperado procesando la solicitud", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocurrió un error inesperado. Si persiste, avisá para revisar los logs del servidor."));
    }
}
