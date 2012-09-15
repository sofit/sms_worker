package sofit.sms_worker;

/**
 * @author evgeny
 */
public class QueueElement {

  private Long id;
  private String recipient;
  private String body;
  private String sendDatetime;

  public QueueElement(Long id, String recipient, String body, String sendDatetime) {
    this.id = id;
    this.recipient = recipient;
    this.body = body;
    this.sendDatetime = sendDatetime;
  }

  public QueueElement(String recipient, String body, String sendDatetime) {
    this(null, recipient, body, sendDatetime);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getRecipient() {
    return recipient;
  }

  public void setRecipient(String recipient) {
    this.recipient = recipient;
  }

  public String getSendDatetime() {
    return sendDatetime;
  }

  public void setSendDatetime(String sendDatetime) {
    this.sendDatetime = sendDatetime;
  }
}
