/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pos.impl;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pos.SearchService;
import com.pos.impl.SearchServiceImpl.Config;

/**
 * Implementation of the SearchService
 */
@Component(service = { SearchService.class })
@Designate(ocd = Config.class)
public class SearchServiceImpl implements SearchService {

	private static final Logger log = LoggerFactory.getLogger(SearchServiceImpl.class);

	@Reference
	private ResourceResolverFactory factory;

	private Config config;

	@ObjectClassDefinition(name = "%cms.reference.search.name", description = "%cms.reference.search.description", localization = "OSGI-INF/l10n/bundle")
	public @interface Config {

		@AttributeDefinition(name = "%searchServiceUsername.name", description = "%searchServiceUsername.description")
		String searchServiceUsername();
	}

	@Activate
	public void init(Config config) {
		this.config = config;
	}

	@Override
	public ResourceResolver getResourceResolver(SlingHttpServletRequest request) {
		if (config != null && StringUtils.isNotBlank(config.searchServiceUsername())) {
			try {
				log.debug("Retrieving Service User {}", config.searchServiceUsername());
				return factory.getServiceResourceResolver(new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;

					{
						put(ResourceResolverFactory.SUBSERVICE, config.searchServiceUsername());
					}
				});
			} catch (LoginException e) {
				log.warn("Failed to retrieve Service User {}, falling back to request user",
						config.searchServiceUsername());
				return request.getResourceResolver();
			}
		} else {
			log.debug("Using request user");
			return request.getResourceResolver();
		}
	}

	@Override
	public void closeResolver(ResourceResolver resolver) {
		if (resolver != null && resolver.isLive() && StringUtils.isNotBlank(config.searchServiceUsername())
				&& config.searchServiceUsername().equals(resolver.getUserID())) {
			resolver.close();
		}
	}

}
