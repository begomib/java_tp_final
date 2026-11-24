USE techlab;
INSERT INTO productos (nombre,descripcion,precio,categoria,imagen,stock,categoria_id)
SELECT 'Bola de cristal navideña','Globo de nieve con casita y pinos en miniatura, sobre una base roja con detalles dorados.',16900,'Adornos','assets/navidad/producto-15.png',12,id FROM categorias WHERE nombre='Adornos' AND NOT EXISTS (SELECT 1 FROM productos WHERE nombre='Bola de cristal navideña');