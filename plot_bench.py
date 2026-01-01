# %%
import pandas as pd
import matplotlib.pyplot as plt

# %%
df = pd.read_csv("bench_results.csv")
df["devices_numeric"] = df["devices"].str.replace("k", "").astype(float) * 1000

# support compact labels like '1k' or '10k'
df["devices_numeric"] = (
    df["devices"]
      .str.replace("k", "", regex=False)
      .astype(float) * 1000
)

# %%
plt.figure()

# use numeric X for plotting
plt.plot(df["devices_numeric"], df["seq_ms_median"], label="Секвентно", marker='o')
plt.plot(df["devices_numeric"], df["par_ms_median"], label="Паралелно", marker='x')

# annotate each point with its Y value
for x, y in zip(df["devices_numeric"], df["seq_ms_median"]):
    plt.annotate(f"{y:.2f} ms", (x, y), textcoords="offset points", xytext=(5, 5))

for x, y in zip(df["devices_numeric"], df["par_ms_median"]):
    plt.annotate(f"{y:.2f} ms", (x, y), textcoords="offset points", xytext=(5, 5))

# pretty X labels: keep 1k, 10k, ...
plt.xticks(df["devices_numeric"], df["devices"])

plt.xlabel("Брой устройства")
plt.ylabel("Време (ms)")
plt.legend()
plt.yscale("log")
plt.title("Време за изпълнение спрямо брой устройства")
plt.grid(True)

plt.savefig("plot_test_1.png", dpi=300)
plt.show()
# %%
