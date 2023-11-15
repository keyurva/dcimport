# Copyright 2023 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import sqlite3

from stats.data import Observation
from stats.data import Triple

_DELETE_TRIPLES_TABLE = "drop table if exists triples;"
_DELETE_OBSERVATIONS_TABLE = "drop table if exists observations;"
_CREATE_TRIPLES_TABLE = """
create table triples (
    subject_id TEXT,
    predicate TEXT,
    object_id TEXT,
    object_value TEXT
);
"""
_CREATE_OBSERVATIONS_TABLE = """
create table observations (
    entity TEXT,
    variable TEXT,
    date TEXT,
    value TEXT,
    provenance TEXT
);
"""

_INIT_SCRIPT = f"""
BEGIN;
{_DELETE_TRIPLES_TABLE}
{_DELETE_OBSERVATIONS_TABLE}
{_CREATE_TRIPLES_TABLE}
{_CREATE_OBSERVATIONS_TABLE}
COMMIT;
"""

_INSERT_TRIPLES_STATEMENT = "insert into triples values(?, ?, ?, ?)"

_INSERT_OBSERVATIONS_STATEMENT = "insert into observations values(?, ?, ?, ?, ?)"


class Db:
  """Class to insert triples and observations into a sqlite DB."""

  def __init__(self, db_file_path: str) -> None:
    self.db_file_path = db_file_path
    self.db = sqlite3.connect(db_file_path)
    self.db.executescript(_INIT_SCRIPT)
    pass

  def insert_triples(self, triples: list[Triple]):
    with self.db:
      self.db.executemany(_INSERT_TRIPLES_STATEMENT,
                          [to_triple_tuple(triple) for triple in triples])

  def insert_observations(self, observations: list[Observation]):
    with self.db:
      self.db.executemany(
          _INSERT_OBSERVATIONS_STATEMENT,
          [to_observation_tuple(observation) for observation in observations])

  def close(self):
    self.db.close()


def to_triple_tuple(triple: Triple):
  return (triple.subject_id, triple.predicate, triple.object_id,
          triple.object_value)


def to_observation_tuple(observation: Observation):
  return (observation.entity, observation.variable, observation.date,
          observation.value, observation.provenance)
