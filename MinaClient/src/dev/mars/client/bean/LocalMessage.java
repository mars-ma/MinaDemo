package dev.mars.client.bean;

/**
 * Created by chen.wenjie on 2016/12/26.
 */

import java.io.Serializable;
/**
 * 定义信息数据结构并利用GreenDao生成DaoMaster,DaoSessioin,LocalMessageDao
 */
//@Entity
public class LocalMessage implements Serializable{
    private static final long serialVersionUID = 1L;
    //@Id
    private Long id;
    private String title;
    private String summary;
    private String content;
    private Integer type;
    private Integer status;
    private Integer level;
    private String recievetime;
    private Integer isfocus;
    private String username;
   //@Generated(hash = 840067488)
    public LocalMessage(Long id, String title, String summary, String content,
                        Integer type, Integer status, Integer level, String recievetime,
                        Integer isfocus, String username) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.type = type;
        this.status = status;
        this.level = level;
        this.recievetime = recievetime;
        this.isfocus = isfocus;
        this.username = username;
    }
   // @Generated(hash = 947300957)
    public LocalMessage() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getSummary() {
        return this.summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getLevel() {
        return this.level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public String getRecievetime() {
        return this.recievetime;
    }
    public void setRecievetime(String recievetime) {
        this.recievetime = recievetime;
    }
    public Integer getIsfocus() {
        return this.isfocus;
    }
    public void setIsfocus(Integer isfocus) {
        this.isfocus = isfocus;
    }
    public String getUserName() { return  this.username; }
    public void setUserName(String username) { this.username = username; }

    @Override
    public String toString() {
        return "id "+getId()+";"
                +"type "+getType()+";"
                +"level "+getLevel()+";"
                +"status "+getStatus()
                ;

    }
}
