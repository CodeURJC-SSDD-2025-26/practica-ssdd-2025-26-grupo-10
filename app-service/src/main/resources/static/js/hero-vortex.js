/* --- EFFECT 1: CUSTOM HERO VORTEX (Canvas API) --- */
(function() {
    const container = document.getElementById('particles-hero');
    if (!container) return;
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    
    canvas.style.width = '100%';
    canvas.style.height = '100%';
    canvas.style.position = 'absolute';
    canvas.style.top = '0';
    canvas.style.left = '0';
    container.appendChild(canvas);

    let width, height;
    let particles = [];
    
    // Configuration
    const particleCount = 150;
    const ringRadius = 250; /* Base radius of the vortex */
    const ringWidth = 100; /* Spread of the ring */
    
    function resize() {
        width = container.offsetWidth;
        height = container.offsetHeight;
        canvas.width = width;
        canvas.height = height;
    }

    class Particle {
        constructor() {
            this.angle = Math.random() * Math.PI * 2;
            this.radius = ringRadius + (Math.random() - 0.5) * ringWidth;
            this.baseRadius = this.radius;
            this.speed = 0.005 + Math.random() * 0.01; /* Rotation speed */
            this.size = 2 + Math.random() * 2;
            this.x = 0;
            this.y = 0;
        }

        update(mouseX, mouseY) {
            // 1. Orbital Movement (Always rotates)
            this.angle += this.speed;
            
            // Base orbital position
            let orbitX = width/2 + Math.cos(this.angle) * this.baseRadius;
            let orbitY = height/2 + Math.sin(this.angle) * this.baseRadius;

            // 2. Mouse Interaction (Local Repulsion/Parting)
            let interactX = 0;
            let interactY = 0;

            if (mouseX !== null) {
                const dx = mouseX - orbitX;
                const dy = mouseY - orbitY;
                const dist = Math.sqrt(dx*dx + dy*dy);
                
                // Interaction Radius: 120px (Local Only)
                if (dist < 120) { 
                     const force = (120 - dist) / 120;
                     const angle = Math.atan2(dy, dx);
                     // Push away from mouse
                     interactX = -Math.cos(angle) * force * 50; 
                     interactY = -Math.sin(angle) * force * 50;
                }
            }

            // Apply position with interaction offset
            this.x = orbitX + interactX;
            this.y = orbitY + interactY;
        }

        draw() {
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(255, 255, 255, ${0.5 + Math.random()*0.5})`; /* Twinkle */
            ctx.fill();
        }
    }

    function init() {
        resize();
        for (let i = 0; i < particleCount; i++) {
            particles.push(new Particle());
        }
        animate();
    }

    // Mouse tracking
    let mouseX = null;
    let mouseY = null;
    window.addEventListener('mousemove', (e) => {
        const rect = canvas.getBoundingClientRect();
        if (e.clientY >= rect.top && e.clientY <= rect.bottom) {
            mouseX = e.clientX - rect.left;
            mouseY = e.clientY - rect.top;
        } else {
            mouseX = null;
        }
    });

    window.addEventListener('resize', resize);

    function animate() {
        ctx.clearRect(0, 0, width, height);
        particles.forEach(p => {
            p.update(mouseX, mouseY);
            p.draw();
        });
        requestAnimationFrame(animate);
    }

    init();
})();
