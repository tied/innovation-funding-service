-- IFS-5989 -- remove per-organisation t&cs and hide from assessment

SET @eoi_competition_type = (SELECT id FROM competition_type ct WHERE ct.name='Expression of interest');

-- we don't want to accept per organisation
UPDATE question q
    SET q.multiple_statuses = FALSE
WHERE
    q.competition_id IN (SELECT c.id FROM competition c WHERE c.competition_type_id = @eoi_competition_type);