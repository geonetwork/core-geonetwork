UPDATE metadata
  SET data = REPLACE(
      data,
      '"Arc seconds (North)"', '"Arc second"')
  WHERE data LIKE '%"Arc seconds (North)"%';
