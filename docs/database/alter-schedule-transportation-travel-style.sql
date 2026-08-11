-- Run against schedule_db before deploying the application that uses these enum values.
-- Review the current values first. Unrecognized legacy values are converted to OTHER.
SELECT transportation, COUNT(*) AS row_count
FROM schedule
GROUP BY transportation;

UPDATE schedule
SET transportation = CASE
    WHEN transportation IS NULL OR TRIM(transportation) = '' THEN NULL
    WHEN UPPER(TRIM(transportation)) = 'TRAIN_PUBLIC_TRANSIT'
         OR TRIM(transportation) IN ('기차', '대중교통', '기차/대중교통', 'KTX')
        THEN 'TRAIN_PUBLIC_TRANSIT'
    WHEN UPPER(TRIM(transportation)) = 'SHIP_FERRY'
         OR TRIM(transportation) IN ('선박', '페리', '선박/페리', '선박페리')
        THEN 'SHIP_FERRY'
    WHEN UPPER(TRIM(transportation)) = 'RENTAL_CAR'
         OR TRIM(transportation) IN ('렌터카', '렌트카')
        THEN 'RENTAL_CAR'
    WHEN UPPER(TRIM(transportation)) = 'WALKING'
         OR TRIM(transportation) = '도보'
        THEN 'WALKING'
    ELSE 'OTHER'
END;

ALTER TABLE schedule
    MODIFY COLUMN transportation ENUM(
        'TRAIN_PUBLIC_TRANSIT',
        'SHIP_FERRY',
        'RENTAL_CAR',
        'WALKING',
        'OTHER'
    ) NULL,
    ADD COLUMN travel_style ENUM(
        'TOUR_LANDMARK',
        'RELAXATION_HEALING',
        'FOOD_TOUR',
        'ACTIVITY',
        'OTHER'
    ) NULL AFTER transportation;

-- Neither column has a UNIQUE constraint, so multiple schedule rows may use the same value.
