package es.urjc.ecomostoles.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MercadoController {

	@GetMapping("/mercado")
	public String mercado(Model model) {
		List<Map<String, String>> ofertas = new ArrayList<>();

		ofertas.add(Map.of(
				"titulo", "Viruta de Acero (500kg)",
				"empresa", "Metales del Sur S.L.",
				"tipo", "Residuo Metálico",
				"descripcion", "Excelente calidad para fundición. Disponible para recogida inmediata.",
				"precio", "€ 1.200",
				"imagenUrl", "/img/virutas.webp"));

		ofertas.add(Map.of(
				"titulo", "Alquiler Montacargas 2T",
				"empresa", "Logística Móstoles S.A.",
				"tipo", "Maquinaria",
				"descripcion", "Disponible por días o semanas. Incluye seguro.",
				"precio", "€ 150/día",
				"imagenUrl", "/img/montacargas.webp"));

		ofertas.add(Map.of(
				"titulo", "Palets de Madera (x200)",
				"empresa", "Muebles García",
				"tipo", "Residuo Madera",
				"descripcion", "Palets usados en buen estado. Se regalan.",
				"precio", "Gratis",
				"imagenUrl", "/img/palés.webp"));
		ofertas.add(Map.of(
				"titulo", "Aceite Industrial (200L)",
				"empresa", "Talleres Pepe",
				"tipo", "Residuo Peligroso",
				"descripcion", "Aceite quemado de motor. Solo gestores autorizados.",
				"precio", "€ 80",
				"imagenUrl", "/img/aceite-industrial.webp"));
		ofertas.add(Map.of(
				"titulo", "Retales de PVC",
				"empresa", "Plásticos Móstoles",
				"tipo", "Residuo Plástico",
				"descripcion", "Recortes de producción limpios. Color blanco.",
				"precio", "Gratis",
				"imagenUrl", "/img/retales-pvc.webp"));
		ofertas.add(Map.of(
				"titulo", "Compartir Nave (50m²)",
				"empresa", "Logística Express",
				"tipo", "Espacio",
				"descripcion", "Cedo espacio sobrante en nave vigilada. Ideal almacenaje.",
				"precio", "€ 300/mes",
				"imagenUrl", "/img/nave.webp"));

		model.addAttribute("ofertas", ofertas);
		return "mercado";
	}
}
