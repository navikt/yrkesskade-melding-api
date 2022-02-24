ALTER TABLE skademelding
    ADD COLUMN kilde VARCHAR(100) DEFAULT ('') NOT NULL,
    ADD COLUMN mottatt_tidspunkt TIMESTAMP;