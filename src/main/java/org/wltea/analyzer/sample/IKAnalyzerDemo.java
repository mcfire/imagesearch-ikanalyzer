/**
 * 
 */
package org.wltea.analyzer.sample;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;
import org.wltea.analyzer.lucene.IKQueryParser;
import org.wltea.analyzer.lucene.IKSimilarity;

/**
 * @author linly
 *
 */
public class IKAnalyzerDemo {
	
	public static void main(String[] args){
		//Lucene Document的域名
		String fieldName = "text";
		 //检索内容
		String text = "IK Analyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";
		
		//实例化IKAnalyzer分词器
		Analyzer analyzer = new IKAnalyzer();
	 
		
		Directory directory = null;
		IndexWriter iwriter = null;
		IndexSearcher isearcher = null;
		try {
			//建立内存索引对象
			directory = new RAMDirectory();	 
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_44, analyzer);
			iwriter = new IndexWriter(directory, config);
			Document doc = new Document();
			doc.add(new StringField("ID", "10000", Field.Store.YES));
			doc.add(new StringField(fieldName, text, Field.Store.YES));
			iwriter.addDocument(doc);
			iwriter.close();
			
		    //实例化搜索器   
			IndexReader reader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(reader);
			//在索引器中使用IKSimilarity相似度评估器
			isearcher.setSimilarity(new IKSimilarity());
			
			String keyword = "中文分词工具包";
			
			//使用IKQueryParser查询分析器构造Query对象
			Query query = IKQueryParser.parse(fieldName, keyword);
			
			//搜索相似度最高的5条记录
			TopDocs topDocs = isearcher.search(query , 5);
			System.out.println("命中：" + topDocs.totalHits);
			//输出结果
			ScoreDoc[] scoreDocs = topDocs.scoreDocs;
			for (int i = 0; i < topDocs.totalHits; i++){
				Document targetDoc = isearcher.doc(scoreDocs[i].doc);
				System.out.println("内容：" + targetDoc.toString());
			}			
			
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(directory != null){
				try {
					directory.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
