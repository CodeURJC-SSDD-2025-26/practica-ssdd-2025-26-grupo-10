package es.urjc.ecomostoles.backend.dto;

import es.urjc.ecomostoles.backend.model.Empresa;
import es.urjc.ecomostoles.backend.model.EstadoOferta;
import java.time.LocalDateTime;

/**
 * Spring Data Projection for Oferta entity to avoid loading BLOB images in listings.
 * Compatible with existing Mustache templates.
 */
public interface OfertaResumen {
    Long getId();
    String getTitulo();
    String getDescripcion();
    String getTipoResiduo();
    String getTipoResiduoFormateado();
    String getCategoria();
    Double getCantidad();
    String getUnidad();
    String getCantidadFormateada();
    Double getPrecio();
    String getPrecioFormateado();
    String getDisponibilidad();
    EstadoOferta getEstado();
    LocalDateTime getFechaPublicacion();
    Empresa getEmpresa();
    int getVisitas();
}
