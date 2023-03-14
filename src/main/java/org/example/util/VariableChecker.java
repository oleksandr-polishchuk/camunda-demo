package org.example.util;

import org.example.domain.ActGeByteArray;
import org.example.domain.ActRuVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VariableChecker {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void check() {
        System.out.println("=========================================================================================");
        var variables = jdbcTemplate.query("select * from act_ru_variable where name_ = 'v1'", new ActRuVariableMapper());
        var byteArrays = jdbcTemplate.query("select * from act_ge_bytearray where name_ = 'v1' and type_ != 3", new ActGeByteArrayMapper());
        System.out.println("V1: variables " + variables.size() + ", bytearrays " + byteArrays.size());

        var varByBytearrayId = variables.stream().collect(Collectors.toMap(ActRuVariable::getBytearrayId, Function.identity()));
        variables.forEach(System.out::println);
        for (ActGeByteArray byteArray : byteArrays) {
            if (varByBytearrayId.containsKey(byteArray.getId())) {
                System.out.println("variable " + varByBytearrayId.get(byteArray.getId()).getId() + " -> " + byteArray);
            } else {
                System.out.println(byteArray);
            }
        }
        System.out.println("=========================================================================================");
    }

    public static class ActGeByteArrayMapper implements RowMapper<ActGeByteArray> {
        @Override
        public ActGeByteArray mapRow(ResultSet rs, int rowNum) throws SQLException {
            var entity = new ActGeByteArray();
            entity.setId(rs.getString("id_"));
            entity.setRev(rs.getInt("rev_"));
            entity.setName(rs.getString("name_"));
            entity.setType(rs.getInt("type_"));
            entity.setCreateTime(rs.getDate("create_time_").toLocalDate());
            entity.setRemovalTime(Optional.ofNullable(rs.getDate("removal_time_")).map(Date::toLocalDate).orElse(null));
            return entity;
        }
    }

    public static class ActRuVariableMapper implements RowMapper<ActRuVariable> {
        @Override
        public ActRuVariable mapRow(ResultSet rs, int rowNum) throws SQLException {
            var entity = new ActRuVariable();
            entity.setId(rs.getString("id_"));
            entity.setRev(rs.getInt("rev_"));
            entity.setType(rs.getString("type_"));
            entity.setName(rs.getString("name_"));
            entity.setExecutionId(rs.getString("execution_id_"));
            entity.setProcInstId(rs.getString("proc_inst_id_"));
            entity.setProcDefId(rs.getString("proc_def_id_"));
            entity.setBytearrayId(rs.getString("bytearray_id_"));
            entity.setText(rs.getString("text_"));
            entity.setText2(rs.getString("text2_"));
            entity.setVarScope(rs.getString("var_scope_"));
            entity.setSequenceCounter(rs.getInt("sequence_counter_"));
            entity.setConcurrentLocal(rs.getBoolean("is_concurrent_local_"));
            entity.setTenantId(rs.getString("tenant_id_"));
            return entity;
        }
    }
}
