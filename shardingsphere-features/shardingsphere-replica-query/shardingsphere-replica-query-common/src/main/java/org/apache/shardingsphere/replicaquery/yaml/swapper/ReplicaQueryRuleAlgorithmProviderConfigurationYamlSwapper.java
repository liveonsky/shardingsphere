/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.replicaquery.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replicaquery.algorithm.config.AlgorithmProvidedReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.rule.ReplicaQueryDataSourceRuleConfiguration;
import org.apache.shardingsphere.replicaquery.constant.ReplicaQueryOrder;
import org.apache.shardingsphere.replicaquery.yaml.config.YamlReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.replicaquery.yaml.config.rule.YamlReplicaQueryDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Replica query rule configuration YAML swapper.
 */
public final class ReplicaQueryRuleAlgorithmProviderConfigurationYamlSwapper
        implements YamlRuleConfigurationSwapper<YamlReplicaQueryRuleConfiguration, AlgorithmProvidedReplicaQueryRuleConfiguration> {
    
    @Override
    public YamlReplicaQueryRuleConfiguration swapToYamlConfiguration(final AlgorithmProvidedReplicaQueryRuleConfiguration data) {
        YamlReplicaQueryRuleConfiguration result = new YamlReplicaQueryRuleConfiguration();
        result.setDataSources(data.getDataSources().stream().collect(
                Collectors.toMap(ReplicaQueryDataSourceRuleConfiguration::getName, this::swapToYamlConfiguration, (oldValue, currentValue) -> oldValue, LinkedHashMap::new)));
        if (null != data.getLoadBalanceAlgorithms()) {
            data.getLoadBalanceAlgorithms().forEach((key, value) -> result.getLoadBalancers().put(key, YamlShardingSphereAlgorithmConfiguration.buildByTypedSPI(value)));
        }
        return result;
    }
    
    private YamlReplicaQueryDataSourceRuleConfiguration swapToYamlConfiguration(final ReplicaQueryDataSourceRuleConfiguration dataSourceRuleConfig) {
        YamlReplicaQueryDataSourceRuleConfiguration result = new YamlReplicaQueryDataSourceRuleConfiguration();
        result.setName(dataSourceRuleConfig.getName());
        result.setPrimaryDataSourceName(dataSourceRuleConfig.getPrimaryDataSourceName());
        result.setReplicaDataSourceNames(dataSourceRuleConfig.getReplicaDataSourceNames());
        result.setLoadBalancerName(dataSourceRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public AlgorithmProvidedReplicaQueryRuleConfiguration swapToObject(final YamlReplicaQueryRuleConfiguration yamlConfig) {
        Collection<ReplicaQueryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        for (Entry<String, YamlReplicaQueryDataSourceRuleConfiguration> entry : yamlConfig.getDataSources().entrySet()) {
            dataSources.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        AlgorithmProvidedReplicaQueryRuleConfiguration ruleConfig = new AlgorithmProvidedReplicaQueryRuleConfiguration();
        ruleConfig.setDataSources(dataSources);
        return ruleConfig;
    }
    
    private ReplicaQueryDataSourceRuleConfiguration swapToObject(final String name, final YamlReplicaQueryDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return new ReplicaQueryDataSourceRuleConfiguration(name, 
                yamlDataSourceRuleConfig.getPrimaryDataSourceName(), yamlDataSourceRuleConfig.getReplicaDataSourceNames(), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    @Override
    public Class<AlgorithmProvidedReplicaQueryRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedReplicaQueryRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "REPLICA_QUERY";
    }
    
    @Override
    public int getOrder() {
        return ReplicaQueryOrder.ALGORITHM_PROVIDER_ORDER;
    }
}
