# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: cache_data.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x10\x63\x61\x63he_data.proto\"\x94\x01\n\rStatVarGroups\x12:\n\x0fstat_var_groups\x18\x01 \x03(\x0b\x32!.StatVarGroups.StatVarGroupsEntry\x1aG\n\x12StatVarGroupsEntry\x12\x0b\n\x03key\x18\x01 \x01(\t\x12 \n\x05value\x18\x02 \x01(\x0b\x32\x11.StatVarGroupNode:\x02\x38\x01\"\xcc\x03\n\x10StatVarGroupNode\x12\x15\n\rabsolute_name\x18\x01 \x01(\t\x12\x32\n\x0f\x63hild_stat_vars\x18\x02 \x03(\x0b\x32\x19.StatVarGroupNode.ChildSV\x12\x39\n\x15\x63hild_stat_var_groups\x18\x03 \x03(\x0b\x32\x1a.StatVarGroupNode.ChildSVG\x12!\n\x19\x64\x65scendent_stat_var_count\x18\x05 \x01(\x05\x12\x1e\n\x16parent_stat_var_groups\x18\x65 \x03(\t\x1ak\n\x08\x43hildSVG\x12\n\n\x02id\x18\x01 \x01(\t\x12\x1a\n\x12specialized_entity\x18\x02 \x01(\t\x12\x14\n\x0c\x64isplay_name\x18\x65 \x01(\t\x12!\n\x19\x64\x65scendent_stat_var_count\x18\x66 \x01(\x05\x1a|\n\x07\x43hildSV\x12\n\n\x02id\x18\x01 \x01(\t\x12\x13\n\x0bsearch_name\x18\x02 \x01(\t\x12\x14\n\x0csearch_names\x18\x04 \x03(\t\x12\x14\n\x0c\x64isplay_name\x18\x03 \x01(\t\x12\x12\n\ndefinition\x18\x05 \x01(\t\x12\x10\n\x08has_data\x18\x65 \x01(\x08J\x04\x08\x04\x10\x05\x62\x06proto3')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'cache_data_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  _STATVARGROUPS_STATVARGROUPSENTRY._options = None
  _STATVARGROUPS_STATVARGROUPSENTRY._serialized_options = b'8\001'
  _STATVARGROUPS._serialized_start=21
  _STATVARGROUPS._serialized_end=169
  _STATVARGROUPS_STATVARGROUPSENTRY._serialized_start=98
  _STATVARGROUPS_STATVARGROUPSENTRY._serialized_end=169
  _STATVARGROUPNODE._serialized_start=172
  _STATVARGROUPNODE._serialized_end=632
  _STATVARGROUPNODE_CHILDSVG._serialized_start=393
  _STATVARGROUPNODE_CHILDSVG._serialized_end=500
  _STATVARGROUPNODE_CHILDSV._serialized_start=502
  _STATVARGROUPNODE_CHILDSV._serialized_end=626
# @@protoc_insertion_point(module_scope)
