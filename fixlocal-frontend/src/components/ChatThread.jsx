function formatDateLabel(date) {
  const now = new Date();
  const target = new Date(date);

  const isSameDay = (a, b) =>
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate();

  const yesterday = new Date();
  yesterday.setDate(now.getDate() - 1);

  if (isSameDay(target, now)) return "Today";
  if (isSameDay(target, yesterday)) return "Yesterday";

  return target.toLocaleDateString(undefined, {
    weekday: "long",
    month: "short",
    day: "numeric",
  });
}

function formatTimeLabel(date) {
  return new Date(date).toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
  });
}

function groupMessagesByDate(messages) {
  return messages.reduce((groups, message) => {
    const label = formatDateLabel(message.createdAt);
    const existing = groups.find((group) => group.label === label);
    if (existing) {
      existing.items.push(message);
    } else {
      groups.push({ label, items: [message] });
    }
    return groups;
  }, []);
}

function ChatThread({ conversation, messages = [], onSend, loading, error }) {
  const disabled = !conversation || loading;
  const groupedMessages = groupMessagesByDate(messages);

  const handleSubmit = (event) => {
    event.preventDefault();
    if (disabled) return;
    const formData = new FormData(event.currentTarget);
    const content = formData.get("message")?.toString().trim();
    const attachment = formData.get("attachment");
    if (!content && (!attachment || attachment.size === 0)) {
      return;
    }
    onSend?.(content || "", attachment && attachment.size ? attachment : undefined);
    event.currentTarget.reset();
  };

  return (
    <div className="bg-white rounded-2xl shadow border border-slate-100 p-4 flex flex-col min-h-[24rem]">
      <div className="flex items-center justify-between mb-3">
        <div>
          <h3 className="text-lg font-semibold">
            {conversation ? conversation.tradespersonName : "Select a conversation"}
          </h3>
          {conversation?.lastMessageAt && (
            <p className="text-xs text-slate-500">
              Last updated {new Date(conversation.lastMessageAt).toLocaleString()}
            </p>
          )}
        </div>
        {loading && <span className="text-xs text-slate-500">Syncing...</span>}
      </div>
      <div className="flex-1 overflow-y-auto space-y-2">
        {error && <p className="text-sm text-red-500">{error}</p>}
        {!error && messages.length === 0 && !loading && (
          <p className="text-slate-500 text-sm">No messages yet.</p>
        )}
        {groupedMessages.map((group) => (
          <div key={group.label} className="space-y-2">
            <div className="text-center text-[11px] uppercase tracking-wide text-slate-400">
              {group.label}
            </div>
            {group.items.map((msg) => (
              <div key={msg.id} className={`flex ${msg.mine ? "justify-end" : "justify-start"}`}>
                <div
                  className={`p-3 rounded-2xl text-sm max-w-[80%] shadow-sm ${
                    msg.mine ? "bg-blue-50" : "bg-slate-100"
                  }`}
                >
                  <p className="whitespace-pre-wrap break-words">{msg.content}</p>
                  {msg.attachment && (
                    <a
                      href={msg.attachment?.url}
                      target="_blank"
                      rel="noreferrer"
                      className="text-xs text-blue-600 block mt-1"
                    >
                      View attachment
                    </a>
                  )}
                  <p
                    className={`mt-1 text-[11px] text-slate-500 ${
                      msg.mine ? "text-right" : "text-left"
                    }`}
                  >
                    {formatTimeLabel(msg.createdAt)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>
      <form className="mt-3 flex flex-col gap-2" onSubmit={handleSubmit}>
        <textarea
          name="message"
          placeholder={conversation ? "Type a message" : "Select a booking conversation"}
          className="border rounded-lg p-2 text-sm min-h-[3rem]"
          disabled={disabled}
        />
        <div className="flex items-center gap-2">
          <input
            type="file"
            name="attachment"
            className="text-xs"
            disabled={disabled}
            accept="image/*,application/pdf"
          />
          <button
            type="submit"
            className="ml-auto bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50"
            disabled={disabled}
          >
            Send
          </button>
        </div>
      </form>
    </div>
  );
}

export default ChatThread;