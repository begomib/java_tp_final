-- Sustituye unicamente las rutas de las ilustraciones del catalogo navideno.
USE techlab;
UPDATE productos SET imagen=REPLACE(imagen,'.svg','.png') WHERE imagen REGEXP '^assets/navidad/producto-[0-9]+[.]svg$';