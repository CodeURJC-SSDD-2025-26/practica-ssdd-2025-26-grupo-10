/* EcoMóstoles Particles Configuration - Clean Light Theme */
document.addEventListener("DOMContentLoaded", function () {
  if (document.getElementById("particles-body")) {
    particlesJS("particles-body", {
      particles: {
        number: { value: 70, density: { enable: true, value_area: 800 } },
        color: { value: "#0a2e23" } /* Dark Jungle Green - High Contrast */,
        shape: { type: "circle" },
        opacity: { value: 0.6, random: false } /* Visible Symbiosis */,
        size: { value: 3, random: true },
        line_linked: {
          enable: true,
          distance: 150,
          color: "#0a2e23",
          opacity: 0.6,
          width: 1.5,
        } /* Thicker lines */,
        move: {
          enable: true,
          speed: 1.5,
          direction: "none",
          random: false,
          straight: false,
          out_mode: "out",
          bounce: false,
        },
      },
      interactivity: {
        detect_on: "window",
        events: {
          onhover: { enable: true, mode: "grab" },
          onclick: { enable: true, mode: "push" },
          resize: true,
        },
        modes: {
          grab: {
            distance: 200,
            line_linked: { opacity: 0.8 },
          } /* Stronger connection */,
          push: { particles_nb: 4 },
        },
      },
      retina_detect: true,
    });
  }
});
