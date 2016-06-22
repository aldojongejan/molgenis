package org.molgenis.data.annotation.entity.impl;

import static org.molgenis.data.annotation.entity.impl.HPORepository.HPO_ID_COL_NAME;
import static org.molgenis.data.annotation.entity.impl.HPORepository.HPO_TERM_COL_NAME;
import static org.molgenis.data.annotator.websettings.HPOAnnotatorSettings.Meta.HPO_LOCATION;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.entity.EntityAnnotator;
import org.molgenis.data.annotation.entity.ResultFilter;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.annotation.resources.impl.RepositoryFactory;
import org.molgenis.data.annotation.resources.impl.ResourceImpl;
import org.molgenis.data.annotation.resources.impl.SingleResourceConfig;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Optional;

/**
 * Typical HPO terms for a gene dataType (already present via SnpEff) Source:
 * http://compbio.charite.de/hudson/job/hpo. annotations.monthly/lastStableBuild/artifact/annotation/
 * ALL_SOURCES_TYPICAL_FEATURES_diseases_to_genes_to_phenotypes .txt
 * 
 * Add resource file path to RuntimeProperty 'hpo_location'
 * 
 */
@Configuration
public class HPOAnnotator
{
	public static final String NAME = "hpo";

	public static final String HPO_IDS = "HPOIDS";
	public static final String HPO_TERMS = "HPOTERMS";

	private static final String HPO_RESOURCE = "HPOResource";

	@Autowired
	private Entity HPOAnnotatorSettings;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	@Bean
	public RepositoryAnnotator hpo()
	{
//		List<AttributeMetaData> attributes = new ArrayList<>();
//		attributes.add(new AttributeMetaData(HPO_IDS).setDataType(MolgenisFieldTypes.TEXT).setDescription(
//				"HPO identifiers"));
//		attributes.add(new AttributeMetaData(HPO_TERMS).setDataType(MolgenisFieldTypes.TEXT).setDescription(
//				"HPO terms"));
//
//		AnnotatorInfo info = AnnotatorInfo
//				.create(Status.READY,
//						Type.PHENOTYPE_ASSOCIATION,
//						NAME,
//						"The Human Phenotype Ontology (HPO) aims to provide a standardized vocabulary of phenotypic abnormalities encountered in human disease."
//								+ "Terms in the HPO describes a phenotypic abnormality, such as atrial septal defect.The HPO is currently being developed using the medical literature, Orphanet, DECIPHER, and OMIM. HPO currently contains approximately 11,000 terms and over 115,000 annotations to hereditary diseases.",
//						attributes);
//
		EntityAnnotator entityAnnotator = null; //FIXME new AnnotatorImpl(HPO_RESOURCE, info, new GeneNameQueryCreator(),
//				new HPOResultFilter(), dataService, resources,
//				new SingleFileLocationCmdLineAnnotatorSettingsConfigurer(HPO_LOCATION, HPOAnnotatorSettings));

		return new RepositoryAnnotatorImpl(entityAnnotator);
	}

	@Bean
	public Resource hpoResource()
	{
		return new ResourceImpl(HPO_RESOURCE, new SingleResourceConfig(HPO_LOCATION, HPOAnnotatorSettings),
				new RepositoryFactory()
				{
					@Override
					public Repository<Entity> createRepository(File file) throws IOException
					{
						return new HPORepository(file);
					}
				});
	}

	public static class HPOResultFilter implements ResultFilter
	{
		@Override
		public Collection<AttributeMetaData> getRequiredAttributes()
		{
			return Collections.emptyList();
		}

		@Override
		public Optional<Entity> filterResults(Iterable<Entity> results, Entity annotatedEntity)
		{
			StringBuilder ids = new StringBuilder();
			StringBuilder terms = new StringBuilder();

			Iterator<Entity> it = results.iterator();
			while (it.hasNext())
			{
				Entity hpoEntity = it.next();
				if (ids.length() > 0)
				{
					ids.append('/');
					terms.append('/');
				}

				String hpoId = hpoEntity.getString(HPO_ID_COL_NAME);
				String hpoTerm = hpoEntity.getString(HPO_TERM_COL_NAME);
				ids.append(hpoId);
				terms.append(hpoTerm);
			}

			Entity aggregated = new DynamicEntity(null); // FIXME pass entity meta data instead of null
			aggregated.set(HPO_IDS, ids.toString());
			aggregated.set(HPO_TERMS, terms.toString());

			return ids.length() == 0 ? Optional.absent() : Optional.of(aggregated);
		}
	}
}