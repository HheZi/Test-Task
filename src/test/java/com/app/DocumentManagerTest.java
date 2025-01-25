package com.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.app.DocumentManager.Author;
import com.app.DocumentManager.Document;
import com.app.DocumentManager.SearchRequest;

@TestInstance(Lifecycle.PER_CLASS)
class DocumentManagerTest {

	private DocumentManager documentManager = new DocumentManager();
	
	@BeforeEach
	public void cleanStorage() {
//		documentManager.documents.clear();
	}
	
	@Test
	@DisplayName("Add document without id")
	void test_add_document_without_id() {
		Document document = Document.builder()
			.author(new DocumentManager.Author("1", "Alex"))
			.content("Some text")
			.created(Instant.now())
			.title("Document").build();
		
		documentManager.save(document);
		
		assertThat(documentManager.documents).contains(document);
	}
	
	@Test
	@DisplayName("Find document by id")
	void test_find_document_by_id() {
		String string = UUID.randomUUID().toString();
		Document document = Document.builder()
				.id(string)
				.author(new DocumentManager.Author("123", "Alex"))
				.content("Some text")
				.created(Instant.now())
				.title("Document").build();
		
		documentManager.save(document);

		
		assertThat(documentManager.findById(string)).isNotEmpty().contains(document);
	}
	
	@Test
	@DisplayName("Find document by id but it is empty")
	void test_find_document_by_id_but_its_empty() {
		assertThat(documentManager.findById("123")).isEmpty();
	}

	@Nested
	@DisplayName("Test search method")
	public class TestSearchMethod{
		
		@Test
		@DisplayName("Find by title prefix")
		void test_find_by_title_prefix() {
			Document document = Document.builder()
					.title("Text 1").build();
			
			Document document2 = Document.builder()
					.title("Some text 2").build();
			
			documentManager.save(document);
			documentManager.save(document2);
			
			assertThat(documentManager
					.search(SearchRequest.builder().titlePrefixes(List.of("Te", "Some")).build())
					).contains(document2, document).hasSize(2);
		}
		
		@Test
		@DisplayName("Find by containing content")
		void test_find_by_containing_content() {
			Document document = Document.builder()
					.content("Text").build();
			
			Document document2 = Document.builder()
					.content("seg").build();
			
			documentManager.save(document);
			documentManager.save(document2);
			
			assertThat(documentManager
					.search(SearchRequest.builder().containsContents(List.of("seg", "Text")).build())
					).contains(document2, document).hasSize(2);
		}
		
		@Test
		@DisplayName("Find by author ids")
		void test_find_by_author_ids() {
			Document document = Document.builder()
					.author(Author.builder().id("3").name("12").build()).build();
			
			Document document2 = Document.builder()
					.author(Author.builder().id("4").name("13").build()).build();
			
			documentManager.save(document);
			documentManager.save(document2);
			
			assertThat(documentManager
					.search(SearchRequest.builder().authorIds(List.of("3", "4")).build())
					).contains(document2, document).hasSize(2);
		}
		
		@Test
		@DisplayName("Find by created from")
		void test_find_by_created_from() {
			Document document = Document.builder()
					.created(Instant.now().plus(2, ChronoUnit.DAYS)).build();
			
			Document document2 = Document.builder()
					.created(Instant.now().plus(14, ChronoUnit.DAYS)).build();
			
			documentManager.save(document);
			documentManager.save(document2);
			
			assertThat(documentManager
					.search(SearchRequest.builder().createdFrom(Instant.now()).build())
					).contains(document2, document).hasSize(2);
		}
		
		@Test
		@DisplayName("Find by created to")
		void test_find_by_created_to() {
			Document document = Document.builder()
					.created(Instant.now().minus(13, ChronoUnit.DAYS)).build();
			
			Document document2 = Document.builder()
					.created(Instant.now().minus(14, ChronoUnit.DAYS)).build();
			
			documentManager.save(document);
			documentManager.save(document2);
			
			assertThat(documentManager
					.search(SearchRequest.builder().createdTo(Instant.now().minus(1,  ChronoUnit.DAYS)).build())
					).contains(document2, document).hasSize(2);
		}
		
		@Test
		@DisplayName("Find by all predicats")
		void test_find_by_all_predicats() {
			Document document = Document.builder()
					.title("Title")
					.content("Content")
					.author(Author.builder().id("a").build())
					.created(Instant.now())
					.build();
			
			
			SearchRequest searchRequest = SearchRequest.builder()
			.titlePrefixes(List.of("T"))
			.containsContents(List.of("Content"))
			.authorIds(List.of("a"))
			.createdFrom(Instant.now().minus(30,  ChronoUnit.DAYS))
			.createdTo(Instant.now().plus(30,  ChronoUnit.DAYS))
			.build();
			
			documentManager.save(document);
			
			assertThat(documentManager
					.search(searchRequest)
					).contains(document).hasSize(1);
		}
	}
	
}
