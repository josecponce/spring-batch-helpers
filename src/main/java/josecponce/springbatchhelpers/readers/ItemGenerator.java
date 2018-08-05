package josecponce.springbatchhelpers.readers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

@AllArgsConstructor
public class ItemGenerator<T> implements ItemReader<T> {
    private Function<Long, T> generator;
    private Long max;

    private final AtomicLong count = new AtomicLong(0);

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Long current = count.getAndIncrement();
        if (current >= max) {
            return null;
        }

        return generator.apply(current);
    }
}
