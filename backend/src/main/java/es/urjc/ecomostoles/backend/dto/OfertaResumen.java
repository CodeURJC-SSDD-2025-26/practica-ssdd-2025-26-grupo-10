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
    @org.springframework.beans.factory.annotation.Value("#{target.tipoResiduoFormateado}")
    String getTipoResiduoFormateado();

    @org.springframework.beans.factory.annotation.Value("#{target.tipoResiduoFormateado}")
    String getCategoria();
    Double getCantidad();
    String getUnidad();
    @org.springframework.beans.factory.annotation.Value("#{target.cantidadFormateada}")
    String getCantidadFormateada();
    Double getPrecio();
    @org.springframework.beans.factory.annotation.Value("#{target.precioFormateado}")
    String getPrecioFormateado();
    String getDisponibilidad();
    EstadoOferta getEstado();
    LocalDateTime getFechaPublicacion();
    Empresa getEmpresa();
    int getVisitas();
}
