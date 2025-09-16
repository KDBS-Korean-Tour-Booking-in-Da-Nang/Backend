-- Enlarge tours.tour_description to MEDIUMTEXT for long HTML content
ALTER TABLE tours
MODIFY COLUMN tour_description MEDIUMTEXT NOT NULL;


