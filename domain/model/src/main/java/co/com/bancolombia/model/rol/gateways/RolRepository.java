package co.com.bancolombia.model.rol.gateways;

import java.util.Optional;

/**
 * Puerto de salida: permite al dominio consultar roles sin conocer la base de datos.
 */
public interface RolRepository {

    Optional<Long> buscarIdPorNombre(String nombre);
}
