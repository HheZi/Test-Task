package com.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;


/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
	
	public final List<Document> documents = new ArrayList<Document>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
    	if (document == null) {
            throw new NullPointerException("Document cannot be null");
        }
    	
    	String id = document.getId();
    	if (id == null || id.isEmpty() || id.isBlank()) {
    		document.setId(UUID.randomUUID().toString());			
		}
    	
    	documents.add(document);
    	
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
    	if (request == null) {
            throw new NullPointerException("SearchRequest cannot be null");
        }
    	
    	return documents.stream()
    	.filter(t -> SearchFilter.filter(t, request))
    	.collect(Collectors.toList());
    }
    
    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
    	return documents
    	.stream()
    	.filter(t -> t.getId().equals(id))
    	.findFirst();
    }

    public static class SearchFilter {
    	
    	private static final List<BiPredicate<Document, SearchRequest>> filters = List.of(
    			SearchFilter::filterByTitlePrefixes,
    	        SearchFilter::filterByContainsContent,
    	        SearchFilter::filterByAuthorIds,
    	        SearchFilter::filterByCreatedFrom,
    	        SearchFilter::filterByCreatedTo
    	    );
    	
    	public static boolean filter(Document document, SearchRequest request) {
    		return filters.stream().allMatch(t -> t.test(document, request));
    	}
    	
    	private static boolean filterByTitlePrefixes(Document document, SearchRequest request) {
        	if (request.getTitlePrefixes() == null) return true;
    			
        	return request
	        	.getTitlePrefixes()
	        	.stream()
	        	.anyMatch(t -> document.getTitle() != null && document.getTitle().startsWith(t));
        }
        
    	private static boolean filterByContainsContent(Document document, SearchRequest request) {
    		if (request.getContainsContents() == null) return true;
    		
    		return request
    			.getContainsContents()
    			.stream()
    			.anyMatch(t -> t.equals(document.getContent()));
        }	
    	
    	private static boolean filterByAuthorIds(Document document, SearchRequest request) {
    		if (request.getAuthorIds() == null) return true;
    		
    		return request
	    		.getAuthorIds()
	    		.stream()
	    		.anyMatch(t -> document.getAuthor() != null && t.equals(document.getAuthor().getId()));
    	}
    	
    	private static boolean filterByCreatedFrom(Document document, SearchRequest request) {
    		if (request.getCreatedFrom() == null) return true;
    		
    		return document.getCreated() != null && document.getCreated().isAfter(request.getCreatedFrom());
    	}
    	
    	private static boolean filterByCreatedTo(Document document, SearchRequest request) {
    		if (request.getCreatedTo() == null) return true;
    		
    		return document.getCreated() != null && document.getCreated().isBefore(request.getCreatedTo());
    	}
    }
    
    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}