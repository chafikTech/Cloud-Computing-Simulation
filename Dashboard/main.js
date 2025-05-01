// Constants
const API_BASE_URL = 'http://localhost:8080/api/cloudsim';
const PRICING_API_URL = 'http://localhost:8080/pricing';

// Chart instances
let resourceChart = null;
let executionChart = null;
let priceChart = null;

// Cloudlet stats for pricing use
let cloudletStats = [];
let allPricing = {};

// Pricing definitions (defaults)
const pricing = {
  aws: { peCost: 0.05, ramCost: 0.01, timeUnit: 3600 },
  azure: { peCost: 0.045, ramCost: 0.012, timeUnit: 3600 },
  gcp: { peCost: 0.043, ramCost: 0.0095, timeUnit: 3600 },
  ibm: { peCost: 0.04, ramCost: 0.011, timeUnit: 3600 }
};

// Initialization

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('runBtn').addEventListener('click', runSimulation);
  initCharts();
  loadPricingAndStats();
});

function initCharts() {
  const resourceCtx = document.getElementById('resourceChart').getContext('2d');
  resourceChart = new Chart(resourceCtx, {
    type: 'bar',
    data: { labels: [], datasets: getResourceDatasets() },
    options: getBarChartOptions('Resource Distribution')
  });

  const executionCtx = document.getElementById('executionChart').getContext('2d');
  executionChart = new Chart(executionCtx, {
    type: 'line',
    data: { labels: [], datasets: getExecutionDatasets() },
    options: getLineChartOptions('Execution Time (s)')
  });

  const priceChartCtx = document.getElementById('priceChart').getContext('2d');
  priceChart = new Chart(priceChartCtx, {
    type: 'bar',
    data: { labels: [], datasets: getPriceDatasets() },
    options: getPriceChartOptions()
  });
}

function getResourceDatasets() {
  return [
    {
      label: 'PEs', backgroundColor: 'rgba(54, 162, 235, 0.5)', borderColor: 'rgba(54, 162, 235, 1)', borderWidth: 1, data: []
    },
    {
      label: 'RAM (GB)', backgroundColor: 'rgba(255, 99, 132, 0.5)', borderColor: 'rgba(255, 99, 132, 1)', borderWidth: 1, data: []
    }
  ];
}

function getExecutionDatasets() {
  return [
    {
      label: 'Execution Time (s)', backgroundColor: 'rgba(75, 192, 192, 0.5)', borderColor: 'rgba(75, 192, 192, 1)', borderWidth: 1, fill: false, data: []
    }
  ];
}

function getPriceDatasets() {
  return [
    {
      label: 'Price Consumed ($)', backgroundColor: 'rgba(153, 102, 255, 0.7)', borderColor: 'rgba(153, 102, 255, 1)', borderWidth: 1, data: []
    }
  ];
}

function getBarChartOptions(title) {
  return {
    responsive: true,
    scales: { y: { beginAtZero: true } }
  };
}

function getLineChartOptions(title) {
  return getBarChartOptions(title);
}

function getPriceChartOptions() {
  return {
    responsive: true,
    plugins: {
      legend: { display: false },
      tooltip: {
        callbacks: {
          label: (context) => `$${context.parsed.y.toFixed(2)}`
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        title: { display: true, text: 'Price in USD' }
      }
    }
  };
}

async function runSimulation() {
  document.getElementById('loadingIndicator').classList.remove('hidden');
  const params = getSimulationParams();

  try {
    const response = await fetch(`${API_BASE_URL}/run?datacenters=${params.datacenters}&hostsPerDc=${params.hostsPerDc}&vms=${params.vms}&cloudlets=${params.cloudlets}`);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    const result = await response.json();
    updateUI(result);
    await fetchAndUpdateStats();
  } catch (error) {
    console.error('Simulation failed:', error);
  } finally {
    document.getElementById('loadingIndicator').classList.add('hidden');
  }
}

function getSimulationParams() {
  return {
    datacenters: document.getElementById('datacenters').value,
    hostsPerDc: document.getElementById('hostsPerDc').value,
    vms: document.getElementById('vms').value,
    cloudlets: document.getElementById('cloudlets').value
  };
}

async function fetchAndUpdateStats() {
  try {
    const [datacenterStats, clStats, summary] = await Promise.all([
      fetch(`${API_BASE_URL}/datacenter-stats`).then(res => res.json()),
      fetch(`${API_BASE_URL}/cloudlet-stats`).then(res => res.json()),
      fetch(`${API_BASE_URL}/summary`).then(res => res.json())
    ]);

    cloudletStats = clStats;
    updateUI(summary);
    updateCharts(datacenterStats, clStats);
    populateTables(datacenterStats, clStats);
    updatePriceChart();
  } catch (error) {
    console.error('Failed to fetch stats:', error);
  }
}

function updateUI(summary) {
  if (!summary || summary.avgExecutionTime === undefined) {
    console.error("Summary data is incomplete", summary);
    return;
  }

  document.getElementById('numDatacenters').textContent = summary.numDatacenters;
  document.getElementById('numHosts').textContent = summary.numHosts;
  document.getElementById('numVms').textContent = summary.numVms;
  document.getElementById('numCloudlets').textContent = summary.totalCloudlets;
  document.getElementById('completedCloudlets').textContent = summary.completedCloudlets;
  document.getElementById('avgExecTime').textContent = summary.avgExecutionTime.toFixed(2);
  document.getElementById('avgWaitTime').textContent = summary.avgWaitTime.toFixed(2);
  document.getElementById('summaryContainer').classList.remove('hidden');
}

function updateCharts(datacenterStats, clStats) {
  resourceChart.data.labels = datacenterStats.map(dc => `Datacenter ${dc.id}`);
  resourceChart.data.datasets[0].data = datacenterStats.map(dc => dc.totalPes);
  resourceChart.data.datasets[1].data = datacenterStats.map(dc => dc.totalRam / (1024 * 1024));
  resourceChart.update();

  executionChart.data.labels = clStats.map(c => `Cloudlet ${c.id}`);
  executionChart.data.datasets[0].data = clStats.map(c => c.executionTime);
  executionChart.update();
}

function populateTables(datacenterStats, clStats) {
  const dcTable = document.getElementById('datacenterTableBody');
  const clTable = document.getElementById('cloudletTableBody');
  dcTable.innerHTML = '';
  clTable.innerHTML = '';

  datacenterStats.forEach(dc => {
    dcTable.innerHTML += `
      <tr>
        <td>${dc.id}</td>
        <td>${dc.numHosts}</td>
        <td>${dc.totalPes}</td>
        <td>${(dc.totalRam / (1024 * 1024) * 10).toFixed(2)}</td>
        <td>${(dc.totalStorage / (1024 * 1024 * 1024) * 90000).toFixed(2)}</td>
        <td>${(dc.totalBandwidth / (1024 * 1024)).toFixed(2)}</td>
      </tr>`;
  });

  clStats.forEach(cl => {
    clTable.innerHTML += `
      <tr>
        <td>${cl.id}</td>
        <td>${cl.vmId}</td>
        <td>${cl.datacenterId}</td>
        <td>${cl.pes}</td>
        <td>${cl.length}</td>
        <td>${cl.startTime.toFixed(2)}</td>
        <td>${cl.finishTime.toFixed(2)}</td>
        <td>${cl.executionTime.toFixed(2)}</td>
        <td>${cl.status}</td>
      </tr>`;
  });
}

function updatePriceChart() {
  const provider = document.getElementById("providerSelect").value;
  const rates = allPricing[provider] || pricing[provider];

  const prices = cloudletStats.map(cl => {
    const execTime = cl.executionTime || cl.execTime || 0;
    const ramGb = cl.ramGb || cl.ram || 0;
    const peCost = cl.pes * (rates.peCost * (execTime / rates.timeUnit));
    const ramCost = ramGb * (rates.ramCost * (execTime / rates.timeUnit));
    return peCost + ramCost;
  });

  priceChart.data.labels = cloudletStats.map(cl => `Cloudlet ${cl.id}`);
  priceChart.data.datasets[0].data = prices;
  priceChart.update();
}

async function loadPricingAndStats() {
  try {
    const pricingResponse = await fetch(PRICING_API_URL);
    const pricingList = await pricingResponse.json();
    pricingList.forEach(p => {
      allPricing[p.provider] = {
        peCost: p.peCost,
        ramCost: p.ramCost,
        timeUnit: 3600
      };
    });
  } catch (e) {
    console.warn("Could not load pricing from API, using defaults.", e);
  }

  await fetchAndUpdateStats();
}